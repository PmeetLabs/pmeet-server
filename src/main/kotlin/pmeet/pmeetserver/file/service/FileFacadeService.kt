package pmeet.pmeetserver.file.service

import org.springframework.stereotype.Service
import pmeet.pmeetserver.file.dto.response.FileUrlResponseDto

@Service
class FileFacadeService(
  private val fileService: FileService
) {

  suspend fun getPreSignedUrlToUpload(fileName: String, fileDomain: String): FileUrlResponseDto {
    return fileService.generatePreSignedUrlToUpload(fileName, fileDomain)
  }

  suspend fun getPreSignedUrlToDownload(objectName: String): FileUrlResponseDto {
    return FileUrlResponseDto.of(fileService.generatePreSignedUrlToDownload(objectName), objectName)
  }
}
