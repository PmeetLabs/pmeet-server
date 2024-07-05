package pmeet.pmeetserver.user.dto.resume.request

import jakarta.validation.constraints.NotBlank

data class DeleteResumeRequestDto(@NotBlank val id: String, val userId: String) {}