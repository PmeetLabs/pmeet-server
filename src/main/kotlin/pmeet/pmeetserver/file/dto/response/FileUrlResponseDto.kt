package pmeet.pmeetserver.file.dto.response

data class FileUrlResponseDto(
  val presignedUrl: String,
  val objectName: String
) {
  companion object {
    fun of(presignedUrl: String, objectName: String): FileUrlResponseDto {
      return FileUrlResponseDto(presignedUrl, objectName)
    }
  }

}
