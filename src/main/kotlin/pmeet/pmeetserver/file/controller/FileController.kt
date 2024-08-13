package pmeet.pmeetserver.file.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.file.dto.response.FileUrlResponseDto
import pmeet.pmeetserver.file.service.FileFacadeService

@RestController
@RequestMapping("/api/v1/file")
class FileController(
  private val fileFacadeService: FileFacadeService
) {

  @GetMapping("/presigned-url/upload")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getPreSignedUrlToUpload(
    @RequestParam("filename") fileName: String,
    @RequestParam("filedomain") fileDomain: String
  ): FileUrlResponseDto {
    return fileFacadeService.getPreSignedUrlToUpload(fileName, fileDomain)
  }


  @GetMapping("/presigned-url/download")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getPreSignedUrlToDownload(@RequestParam("objectname") objectName: String): FileUrlResponseDto {
    return fileFacadeService.getPreSignedUrlToDownload(objectName)
  }
}
