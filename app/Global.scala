import com.google.inject.{AbstractModule, Guice}
import daos._
import play.api.{Application, GlobalSettings, Play}
import services.{FileUtil, LotteryService, LotteryServiceImpl}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    createFileDirs()
  }

  val injector = Guice.createInjector(new AbstractModule {
    protected def configure() {
      bind(classOf[UnboundGiftDAO]).to(classOf[UnboundGiftDAOImpl])
      bind(classOf[ContributorInfoDAO]).to(classOf[ContributorInfoDAOImpl])
      bind(classOf[LotteryParticipationDAO]).to(classOf[LotteryParticipationDAOImpl])
      bind(classOf[TicketDAO]).to(classOf[TicketDAOImpl])
      bind(classOf[PrizeDAO]).to(classOf[PrizeDAOImpl])
      bind(classOf[LotteryService]).to(classOf[LotteryServiceImpl])
    }
  })

  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)

  def createFileDirs(){
    Play.current.getFile(FileUtil.FILES_DIR).mkdir()
  }
}
