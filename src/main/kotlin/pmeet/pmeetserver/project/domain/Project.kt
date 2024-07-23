package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


data class Recruitment(
  val jobName: String,
  var numberOfRecruitment: Int,
)

data class ProjectBookmark(
  val userId: String,
  val addedAt: LocalDateTime
)

@Document
class Project(

  @Id
  var id: String? = null, // mongodb auto id generation
  val userId: String,
  var title: String,
  var startDate: LocalDateTime,
  var endDate: LocalDateTime,
  var thumbNailUrl: String? = null,
  var techStacks: List<String>? = listOf(),
  var recruitments: List<Recruitment>,
  var description: String,
  var isCompleted: Boolean = false,
  var bookmarkers: MutableList<ProjectBookmark> = mutableListOf(), // 북마크를 한 유저 ID 리스트
  val createdAt: LocalDateTime = LocalDateTime.now(),
  var updatedAt: LocalDateTime = LocalDateTime.now() // 조회 시 정렬을 위해 now()로 초기화
) {

  fun update(
    title: String? = null,
    startDate: LocalDateTime? = null,
    endDate: LocalDateTime? = null,
    thumbNailUrl: String? = null,
    techStacks: List<String>? = null,
    recruitments: List<Recruitment>? = null,
    description: String? = null
  ) = apply {
    title?.let { this.title = it }
    startDate?.let { this.startDate = it }
    endDate?.let { this.endDate = it }
    thumbNailUrl?.let { this.thumbNailUrl = it }
    techStacks?.let { this.techStacks = it }
    recruitments?.let { this.recruitments = it }
    description?.let { this.description = it }
    this.updatedAt = LocalDateTime.now()
  }

  fun addBookmark(userId: String) {
    val newBookMark = ProjectBookmark(userId, LocalDateTime.now())
    bookmarkers.indexOfFirst { it.userId == userId }
      .takeIf { it != -1 }
      ?.let { bookmarkers[it] = newBookMark }
      ?: bookmarkers.add(newBookMark)
  }

  fun deleteBookmark(userId: String) {
    bookmarkers.removeIf { it.userId == userId }
  }

}
