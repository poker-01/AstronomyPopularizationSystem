package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.UserPublicProfileDto;
import com.springboot.backendserver.entity.Follow;
import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.repository.FollowRepository;
import com.springboot.backendserver.repository.PostRepository;
import com.springboot.backendserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public FollowService(FollowRepository followRepository,
                         UserRepository userRepository,
                         PostRepository postRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public UserPublicProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw BusinessException.notFound("用户不存在");
        }

        long followerCount = followRepository.countByFolloweeId(userId);
        long followingCount = followRepository.countByFollowerId(userId);
        long postCount = postRepository.countByUserIdAndStatus(userId, ModerationStatus.APPROVED);

        User viewer = AuthContext.get();
        Boolean following = viewer != null
                && followRepository.existsByFollowerIdAndFolloweeId(viewer.getId(), userId);

        return UserPublicProfileDto.from(user, followerCount, followingCount, postCount, following);
    }

    @Transactional
    public void follow(Long followeeId) {
        User follower = AuthContext.require();
        if (follower.getId().equals(followeeId)) {
            throw BusinessException.badRequest("不能关注自己");
        }
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (Boolean.TRUE.equals(followee.getDeleted())) {
            throw BusinessException.notFound("用户不存在");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followeeId)) {
            return;
        }
        Follow follow = new Follow();
        follow.setFollowerId(follower.getId());
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followeeId) {
        User follower = AuthContext.require();
        followRepository.deleteByFollowerIdAndFolloweeId(follower.getId(), followeeId);
    }
}
