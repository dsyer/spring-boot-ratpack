@Grab('spring-boot-starter-actuator')
import java.lang.String // Yes, a @Grab has to annotate something

ratpack {
  handlers {
    get {
      render json([msg:'Hello'])
    }
  }
}
