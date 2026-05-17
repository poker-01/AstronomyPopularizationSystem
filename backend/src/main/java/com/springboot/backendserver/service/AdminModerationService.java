package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.ModerationItemDto;
import com.springboot.backendserver.dto.ModerationRejectRequest;
import com.springboot.backendserver.entity.Comment;
import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.Post;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.CommentRepository;
import com.springboot.backendserver.repository.PostRepository;
import com.springboot.backendserver.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminModerationService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public AdminModerationService(PostRepository postRepository,
                                  CommentRepository commentRepository,
                                  UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public PageResult<ModerationItemDto> listPendingPosts(int page, int size) {
        return listPostsByStatus(ModerationStatus.PENDING, page, size);
    }

    public PageResult<ModerationItemDto> listPosts(int page, int size, ModerationStatus status) {
        ModerationStatus filter = status != null ? status : ModerationStatus.PENDING;
        return listPostsByStatus(filter, page, size);
    }

    private PageResult<ModerationItemDto> listPostsByStatus(ModerationStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Post> result = postRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        Map<Long, User> users = loadUsers(result.getContent().stream().map(Post::getUserId).collect(Collectors.toSet()));
        List<ModerationItemDto> items = result.getContent().stream()
                .map(p -> ModerationItemDto.fromPost(p, PostService.displayName(users.get(p.getUserId()))))
                .toList();
        return PageResult.of(items, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public PageResult<ModerationItemDto> listComments(int page, int size, ModerationStatus status) {
        ModerationStatus filter = status != null ? status : ModerationStatus.PENDING;
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Comment> result = commentRepository.findByStatusOrderByCreatedAtDesc(filter, pageable);

        Set<Long> userIds = result.getContent().stream().map(Comment::getUserId).collect(Collectors.toSet());
        Set<Long> postIds = result.getContent().stream().map(Comment::getPostId).collect(Collectors.toSet());
        Map<Long, User> users = loadUsers(userIds);
        Map<Long, Post> posts = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        List<ModerationItemDto> items = result.getContent().stream()
                .map(c -> {
                    Post post = posts.get(c.getPostId());
                    String postTitle = post != null ? post.getTitle() : "已删除帖子";
                    return ModerationItemDto.fromComment(c, PostService.displayName(users.get(c.getUserId())), postTitle);
                })
                .toList();
        return PageResult.of(items, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    @Transactional
    public void approvePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        post.setStatus(ModerationStatus.APPROVED);
        post.setRejectReason(null);
        postRepository.save(post);
    }

    @Transactional
    public void rejectPost(Long id, ModerationRejectRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("帖子不存在"));
        post.setStatus(ModerationStatus.REJECTED);
        post.setRejectReason(request != null && StringUtils.hasText(request.getRejectReason())
                ? request.getRejectReason().trim() : "内容不符合社区规范");
        postRepository.save(post);
    }

    @Transactional
    public void approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));
        if (comment.getStatus() == ModerationStatus.APPROVED) {
            return;
        }
        comment.setStatus(ModerationStatus.APPROVED);
        comment.setRejectReason(null);
        commentRepository.save(comment);

        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setCommentCount(commentRepository.countByPostIdAndStatus(post.getId(), ModerationStatus.APPROVED));
            postRepository.save(post);
        });
    }

    @Transactional
    public void rejectComment(Long id, ModerationRejectRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("评论不存在"));
        comment.setStatus(ModerationStatus.REJECTED);
        comment.setRejectReason(request != null && StringUtils.hasText(request.getRejectReason())
                ? request.getRejectReason().trim() : "内容不符合社区规范");
        commentRepository.save(comment);
    }

    private Map<Long, User> loadUsers(Set<Long> ids) {
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
    }
}
