package pmeet.pmeetserver.project.dto.response

data class GetMyInReviewProjectResponseDto(
  val id: String,
  val title: String,
  val description: String,
  val thumbNailUrl: String?,
  val positionName: String?,
) {
  companion object {
    fun of(
      id: String,
      title: String,
      description: String,
      thumbNailDownloadUrl: String?,
      positionName: String?,
    ): GetMyInReviewProjectResponseDto {
      return GetMyInReviewProjectResponseDto(
        id = id,
        title = title,
        description = description,
        thumbNailUrl = thumbNailDownloadUrl,
        positionName = positionName,
      )
    }
  }
}
