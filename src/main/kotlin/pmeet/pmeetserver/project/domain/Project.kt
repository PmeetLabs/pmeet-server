package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


data class Recruitment(
  val jobName: String,
  var numberOfRecruitment: Int,
)

@Document
class Project(

  @Id
  val id: String? = null, // mongodb auto id generation
  val userId: String,
  var title: String,
  var startDate: LocalDateTime,
  var endDate: LocalDateTime,
  var thumbNailUrl: String? = null,
  var techStacks: List<String>? = mutableListOf(),
  var recruitments: List<Recruitment>,
  var description: String,
  var isCompleted: Boolean = false,
  var bookMarkers: List<String> = mutableListOf(), // 북마크를 한 유저 ID 리스트
  val createdAt: LocalDateTime = LocalDateTime.now()
) {
}
