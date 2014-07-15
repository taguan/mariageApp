import com.google.inject.{AbstractModule, Guice}
import daos.{ContributorInfoDAOImpl, ContributorInfoDAO, UnboundGiftDAO, UnboundGiftDAOImpl}
import play.api.GlobalSettings

object Global extends GlobalSettings {

  val injector = Guice.createInjector(new AbstractModule {
    protected def configure() {
      bind(classOf[UnboundGiftDAO]).to(classOf[UnboundGiftDAOImpl])
      bind(classOf[ContributorInfoDAO]).to(classOf[ContributorInfoDAOImpl])
    }
  })

  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)
}
