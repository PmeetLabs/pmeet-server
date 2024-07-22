package pmeet.pmeetserver.project.domain.enum

enum class ProjectTryoutStatus(val description: String) {
  ACCEPTED("합격"),
  REJECTED("불합격"),
  INREVIEW("검토중")
  ;
}