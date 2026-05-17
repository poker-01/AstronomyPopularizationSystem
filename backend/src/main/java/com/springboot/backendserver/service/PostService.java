package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.*;
import com.springboot.backendserver.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository,
                       CommentRepository commentRepository,
                       UserRepository userRepository,
                       FollowRepository followRepository,
                       PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public PageResult<PostSummaryDto> listPosts(int page, int size, Boolean following, Long userId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Post> result;
        if (Boolean.TRUE.equals(following)) {
            User viewer = AuthContext.require();
            List<Long> followeeIds = followRepository.findByFollowerId(viewer.getId()).stream()
                    .map(Follow::getFolloweeId)
                    .toList();
            if (followeeIds.isEmpty()) {
                return PageResult.of(List.of(), 0, 0, safePage, safeSize);
            }
            result = postRepository.findApprovedByUserIds(ModerationStatus.APPROVED, followeeIds, pageable);
        } else if (userId != null) {
            result = postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ModerationStatus.APPROVED, pageable);
        } else {
            result = postRepository.findByStatusOrderByCreatedAtDesc(ModerationStatus.APPROVED, pageable);
        }

        Map<Long, User> users = loadUsers(result.getContent());
        List<PostSummaryDto> items = result.getContent().stream()
                .map(p -> PostSummaryDto.from(p, displayName(users.get(p.getUserId())), avatar(users.get(p.getUserId()))))
                .toList();
        return PageResult.of(items, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public PageResult<PostSummaryDto> listMyPending(int page, int size) {
        User user = AuthContext.require();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Post> result = postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                user.getId(), ModerationStatus.PENDING, pageable);
        List<PostSummaryDto> items = result.getContent().stream()
                .map(p -> PostSummaryDto.from(p, displayName(user), avatar(user)))
                .toList();
        return PageResult.of(items, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public PostDetailDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        User author = userRepository.findById(post.getUserId())
                .orElseThrow(() -> BusinessException.notFound("作者不存在"));

        User viewer = AuthContext.get();
        boolean isAuthor = viewer != null && viewer.getId().equals(post.getUserId());
        if (post.getStatus() != ModerationStatus.APPROVED && !isAuthor) {
            throw BusinessException.notFound("帖子不存在");
        }

        Boolean likedByMe = viewer != null && postLikeRepository.existsByPostIdAndUserId(id, viewer.getId());
        return PostDetailDto.from(post, displayName(author), avatar(author), likedByMe);
    }

    @Transactional
    public PostDetailDto createPost(PostCreateRequest request) {
        User user = AuthContext.require();
        if (request == null || !StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw BusinessException.badRequest("标题和内容不能为空");
        }
        String title = request.getTitle().trim();
        String content = request.getContent().trim();
        if (title.length() > 200) {
            throw BusinessException.badRequest("标题不能超过 200 字");
        }

        Post post = new Post();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setStatus(ModerationStatus.PENDING);
        post = postRepository.save(post);
        return PostDetailDto.from(post, displayName(user), avatar(user), false);
    }

    public List<CommentDto> listComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        User viewer = AuthContext.get();
        boolean isAuthor = viewer != null && viewer.getId().equals(post.getUserId());
        if (post.getStatus() != ModerationStatus.APPROVED && !isAuthor) {
            throw BusinessException.notFound("帖子不存在");
        }

        List<Comment> comments = commentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, ModerationStatus.APPROVED);
        Map<Long, User> users = loadUsersForComments(comments);
        Map<Long, CommentDto> map = new LinkedHashMap<>();
        List<CommentDto> roots = new ArrayList<>();

        for (Comment c : comments) {
            User u = users.get(c.getUserId());
            CommentDto dto = CommentDto.from(c, displayName(u), avatar(u));
            map.put(c.getId(), dto);
        }
        for (Comment c : comments) {
            CommentDto dto = map.get(c.getId());
            if (c.getParentId() == null) {
                roots.add(dto);
            } else {
                CommentDto parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.getReplies().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    @Transactional
    public CommentDto createComment(Long postId, CommentCreateRequest request) {
        User user = AuthContext.require();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        if (post.getStatus() != ModerationStatus.APPROVED) {
            throw BusinessException.badRequest("该帖子暂不可评论");
        }
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw BusinessException.badRequest("评论内容不能为空");
        }

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> BusinessException.badRequest("父评论不存在"));
            if (!parent.getPostId().equals(postId)) {
                throw BusinessException.badRequest("父评论不属于该帖子");
            }
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(request.getContent().trim());
        comment.setParentId(request.getParentId());
        comment.setStatus(ModerationStatus.PENDING);
        comment = commentRepository.save(comment);
        return CommentDto.from(comment, displayName(user), avatar(user));
    }

    @Transactional
    public void likePost(Long postId) {
        User user = AuthContext.require();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        if (post.getStatus() != ModerationStatus.APPROVED) {
            throw BusinessException.badRequest("该帖子暂不可点赞");
        }
        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return;
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(user.getId());
        postLikeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        User user = AuthContext.require();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        if (!postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return;
        }
        postLikeRepository.deleteByPostIdAndUserId(postId, user.getId());
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    private Map<Long, User> loadUsers(List<Post> posts) {
        Set<Long> ids = posts.stream().map(Post::getUserId).collect(Collectors.toSet());
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    private Map<Long, User> loadUsersForComments(List<Comment> comments) {
        Set<Long> ids = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    static String displayName(User user) {
        if (user == null) return "未知用户";
        if (StringUtils.hasText(user.getNickname())) return user.getNickname();
        return user.getUsername();
    }

    static String avatar(User user) {
        return user != null ? user.getAvatar() : null;
    }
}
