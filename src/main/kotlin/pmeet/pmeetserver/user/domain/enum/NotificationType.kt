package pmeet.pmeetserver.user.domain.enum

enum class NotificationType(val description: String, val message: String) {

  APPLY("지원", "나의 프밋모집 글에 지원자가 있습니다."),
  ACCEPTED("합격", "새로운 프밋이 시작되었습니다."),
  REJECTED("불합격", "새로운 댓글이 작성되었습니다."),
  COMMENT("댓글", "내 댓글에 답글이 작성되었습니다."),
  REPLY("답글", "지원한 프밋 모집이 마감되었습니다."),

}
