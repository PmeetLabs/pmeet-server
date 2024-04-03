package pmeet.pmeetserver.file.dto.response

data class FileUrlResponseDto(
  val presignedUrl: String
) {
  companion object {
    fun from(presignedUrl: String): FileUrlResponseDto {
      return FileUrlResponseDto(presignedUrl)
    }
  }

}
