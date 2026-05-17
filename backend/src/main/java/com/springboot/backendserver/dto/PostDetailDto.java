package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDetailDto {

    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private ModerationStatus status;
    private String rejectReason;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
    private LocalDateTime createdAt;

    public static PostDetailDto from(Post post, String authorName, String authorAvatar, Boolean likedByMe) {
        PostDetailDto dto = new PostDetailDto();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setAuthorName(authorName);
        dto.setAuthorAvatar(authorAvatar);
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setRejectReason(post.getRejectReason());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setLikedByMe(likedByMe);
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
}
