package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Comment;
import com.springboot.backendserver.entity.ModerationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDto {

    private Long id;
    private Long postId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private Long parentId;
    private ModerationStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private List<CommentDto> replies = new ArrayList<>();

    public static CommentDto from(Comment comment, String authorName, String authorAvatar) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setAuthorName(authorName);
        dto.setAuthorAvatar(authorAvatar);
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParentId());
        dto.setStatus(comment.getStatus());
        dto.setRejectReason(comment.getRejectReason());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
