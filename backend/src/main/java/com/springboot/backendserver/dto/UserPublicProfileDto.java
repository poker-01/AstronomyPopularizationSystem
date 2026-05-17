package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPublicProfileDto {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;
    private long followerCount;
    private long followingCount;
    private long postCount;
    private Boolean following;

    public static UserPublicProfileDto from(User user, long followerCount, long followingCount,
                                            long postCount, Boolean following) {
        UserPublicProfileDto dto = new UserPublicProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setFollowerCount(followerCount);
        dto.setFollowingCount(followingCount);
        dto.setPostCount(postCount);
        dto.setFollowing(following);
        return dto;
    }
}
