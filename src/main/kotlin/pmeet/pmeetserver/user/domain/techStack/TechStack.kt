package pmeet.pmeetserver.user.domain.techStack

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id

@Document
class TechStack(@Id val id: String? = null, val name: String) {}