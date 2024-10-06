package pmeet.pmeetserver.file.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import pmeet.pmeetserver.file.dto.response.FileUrlResponseDto
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.LocalDate
import java.util.*


@Service
class FileService(
  @Value("\${amazon.aws.bucket}") private val bucketName: String,
  @Value("\${amazon.aws.region}") private val region: String,
  private val awsCredentialsProvider: AwsCredentialsProvider,
) {

  companion object {
    private const val DURATION_OF_PRESIGNED_URL_MINUTE = 30L
  }

  suspend fun generatePreSignedUrlToUpload(fileName: String, fileDomain: String): FileUrlResponseDto {
    val encodedFileName = "${createUUID()}_${fileName}"
    val objectName = "${LocalDate.now()}/$fileDomain/$encodedFileName"
    S3Presigner.builder()
      .credentialsProvider(awsCredentialsProvider)
      .region(Region.of(region))
      .build().use { presigner ->
        PutObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(DURATION_OF_PRESIGNED_URL_MINUTE))
          .putObjectRequest(
            PutObjectRequest.builder()
              .bucket(bucketName)
              .key(objectName)
              .build()
          )
          .build().run {
            return FileUrlResponseDto.of(
              presigner.presignPutObject(this).url().toExternalForm(),
              objectName
            )
          }
      }
  }

  suspend fun generatePreSignedUrlToDownload(objectName: String): String? {
    if (objectName.isBlank()) {
      return null
    }

    S3Presigner.builder()
      .credentialsProvider(awsCredentialsProvider)
      .region(Region.of(region))
      .build().use { presigner ->
        GetObjectPresignRequest.builder()
          .signatureDuration(Duration.ofMinutes(DURATION_OF_PRESIGNED_URL_MINUTE))
          .getObjectRequest(
            GetObjectRequest.builder()
              .bucket(bucketName)
              .key(objectName)
              .build()
          )
          .build().run {
            return presigner.presignGetObject(this).url().toExternalForm()
          }
      }
  }

  suspend fun generatePreSignedUrlsToDownload(objectNames: List<String>): List<String>? {
    return objectNames.takeIf { it.isNotEmpty() }?.mapNotNull { generatePreSignedUrlToDownload(it) }
  }

  /**
   * 파일 고유 ID를 생성
   * @return 36자리의 UUID
   */
  private suspend fun createUUID() = UUID.randomUUID().toString()

}
