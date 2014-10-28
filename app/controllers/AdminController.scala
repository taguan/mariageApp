package controllers

import com.google.inject.{Inject, Singleton}
import daos.{LotteryParticipationDAO, PrizeDAO, TicketDAO}
import models._
import play.api.Play
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db.DB
import play.api.mvc._
import services.{FileUtil, LotteryService, Mailer}

import scala.concurrent.Future

@Singleton
class AdminController @Inject() (lotteryParticipationDAO : LotteryParticipationDAO, ticketDAO : TicketDAO, lotteryService : LotteryService, prizeDAO : PrizeDAO) extends Controller{
  def viewGifts() = Authenticated{ implicit request =>
    val giftsAndTickets : List[Either[LotteryParticipationWithTickets,UnboundGift]]  = DB.withConnection {
      implicit connection => lotteryParticipationDAO.getAll().map {
        case g: UnboundGift => Right(g)
        case l: LotteryParticipation => Left(LotteryParticipationWithTickets(l, ticketDAO.tickets(l)))
      }
    }
    Ok(views.html.adminParticipations(giftsAndTickets.filter(_.isLeft).map(_.left.get), giftsAndTickets.filter(_.isRight).map(_.right.get)))
  }

  def viewUnconfirmedParticipations() = Authenticated{ implicit request =>
    DB.withConnection { implicit connection =>
      Ok(views.html.adminUnconfirmed(lotteryParticipationDAO.unconfirmedParticipations()))
    }
  }

  def confirmParticipation(code : String) = Authenticated{ implicit request =>
    DB.withTransaction{ implicit connection =>
      val participation: LotteryParticipation = lotteryParticipationDAO.getParticipation(code)
      lotteryService.attributeTickets(participation)
      participation.contributorInfo.emailAddress.map(mail => Mailer.sendMail(mail :: Nil, "Ticket accepté", views.html.confirmationMail(participation)))
    }
    Redirect(routes.AdminController.viewUnconfirmedParticipations())
  }

  def manageWinningDefinitions = Authenticated{ implicit request => DB.withConnection{ implicit connection =>
    Ok(views.html.winningDefinitions(prizeDAO.getWinningDefinitions()))
    }
  }

  val winningDefinitionForm = Form(tuple("name" -> nonEmptyText(), "quantity" -> number))

  def newWinningDefinition = Authenticated{ implicit request =>
    Ok(views.html.newWinningDef(winningDefinitionForm))
  }

  def createWinningDefinition = Authenticated { implicit request => DB.withConnection { implicit connection =>
    var errorResult : Option[Result] = None
    winningDefinitionForm.bindFromRequest().fold(
      formWithErrors => errorResult = Some(Redirect(routes.AdminController.newWinningDefinition()).flashing(
        "error" -> "erreur dans le formulaire"
      )),
      winningDefTuple => {
        val winningPrize : WinningPrizeDefinition = prizeDAO.createWinningPrize(winningDefTuple._1, winningDefTuple._2)
        request.body.asMultipartFormData.map(multipartForm => {
          multipartForm.file("image").map { image =>
            FileUtil.getExtension(image.contentType).map { extension =>
              image.ref.moveTo(FileUtil.imagePath(winningPrize.id), replace = true)
            }.getOrElse {
              errorResult = Some(Redirect(routes.AdminController.newWinningDefinition()).flashing(
                "error" -> "mauvais type d'image (png obligatoire)"
              ))
            }
          }
          multipartForm.file("pdf").map { pdf =>
            FileUtil.getPDFExtension(pdf.contentType).map { extension =>
              pdf.ref.moveTo(FileUtil.pdfPath(winningPrize.id), replace = true)
            }.getOrElse {
              errorResult = Some(Redirect(routes.AdminController.newWinningDefinition()).flashing(
                "error" -> "pas un pdf"
              ))
            }
          }})
      })

    errorResult.map(error => error).getOrElse(Redirect(routes.AdminController.manageWinningDefinitions()).flashing(
      "success" -> "prix créé avec succès"
    ))
  }}

  def showEditWinningDefinition(id : Long) = Authenticated { implicit request => DB.withConnection { implicit connection =>
    DB.withConnection{ implicit connection =>
      Ok(views.html.editWinningDef(winningDefinitionForm, prizeDAO.getWinningPrize(id)))
    }
  }}

  def updateWinningDefinition(id : Long) = Authenticated { implicit request => DB.withConnection { implicit connection =>
    var errorResult : Option[Result] = None
    winningDefinitionForm.bindFromRequest().fold(
      formWithErrors => errorResult = Some(Redirect(routes.AdminController.showEditWinningDefinition(id)).flashing(
        "error" -> "erreur dans le formulaire"
      )),
      winningDefTuple => {
        prizeDAO.updatePrize(id,winningDefTuple._1, winningDefTuple._2)
        request.body.asMultipartFormData.map(multipartForm => {
          multipartForm.file("image").map { image =>
            FileUtil.getExtension(image.contentType).map { extension =>
              image.ref.moveTo(FileUtil.imagePath(id), replace = true)
            }.getOrElse {
              errorResult = Some(Redirect(routes.AdminController.showEditWinningDefinition(id)).flashing(
                "error" -> "mauvais type d'image (png obligatoire)"
              ))
            }
          }
          multipartForm.file("pdf").map { pdf =>
            FileUtil.getPDFExtension(pdf.contentType).map { extension =>
              pdf.ref.moveTo(FileUtil.pdfPath(id), replace = true)
            }.getOrElse {
              errorResult = Some(Redirect(routes.AdminController.showEditWinningDefinition(id)).flashing(
                "error" -> "pas un pdf"
              ))
            }
          }})
      })

    errorResult.map(error => error).getOrElse(Redirect(routes.AdminController.manageWinningDefinitions()).flashing(
      "success" -> "prix édité avec succès"
    ))
  }}

  val lostDefinitionForm = Form(single("probability" -> number))

  def manageLostDefinition = Authenticated{ implicit request =>
    DB.withConnection{ implicit connection =>
      Ok(views.html.lostDefinition(lostDefinitionForm, prizeDAO.getLostDefinition()))
    }
  }


  def updateLostDefinition()  = Authenticated { implicit request => DB.withConnection{implicit connection =>
    var errorResult : Option[Result] = None
    lostDefinitionForm.bindFromRequest().fold(
      formWithErrors => errorResult = Some(Redirect(routes.AdminController.manageLostDefinition()).flashing(
        "error" -> "mauvaise probabilité"
      )),
      probability => {
        val lostDefinition =  prizeDAO.getLostDefinition()
        request.body.asMultipartFormData.map(multipartForm => {
          multipartForm.file("image").map { image =>
            FileUtil.getExtension(image.contentType).map { extension =>
              image.ref.moveTo(FileUtil.imagePath(lostDefinition.id), replace = true)
            }.getOrElse {
               errorResult = Some(Redirect(routes.AdminController.manageLostDefinition()).flashing(
                "error" -> "mauvais type d'image (png obligatoire)"
              ))
            }
          }
          multipartForm.file("pdf").map { pdf =>
            FileUtil.getPDFExtension(pdf.contentType).map { extension =>
              pdf.ref.moveTo(FileUtil.pdfPath(lostDefinition.id), replace = true)
            }.getOrElse {
              errorResult = Some(Redirect(routes.AdminController.manageLostDefinition()).flashing(
                "error" -> "pas un pdf"
              ))
            }
        }})
        if(probability != lostDefinition.probability) prizeDAO.updateLostProbability(probability)
      })

    errorResult.map(error => error).getOrElse(Redirect(routes.AdminController.manageLostDefinition()).flashing(
      "success" -> "mise à jour ok"
    ))
    }
  }

  def sendImage(definitionId : Long) = Authenticated{ implicit request =>
    try
      Ok.sendFile(FileUtil.imagePath(definitionId), inline = true)
    catch{
        case e : Exception =>
          Ok
      }
  }

  def sendPdf(definitionId : Long) = Authenticated{ implicit request =>
    try
      Ok.sendFile(FileUtil.pdfPath(definitionId), inline = false)
    catch{
      case e : Exception =>
        Ok
    }
  }


  def showLogin = Action{ implicit request =>
    Ok(views.html.login())
  }

  val loginForm = Form(single("login" -> nonEmptyText().verifying(login => login == Play.current.configuration.getString("admin.password").getOrElse("password"))))

  def login = Action{ implicit request =>
    loginForm.bindFromRequest().fold(
      formWithErrors => Redirect(routes.AdminController.showLogin()),
    login => Redirect(routes.AdminController.manageWinningDefinitions()).withSession(
      "connected" -> "yes")
    )
  }

  object Authenticated extends ActionFilter[Request] with ActionBuilder[Request] {
    override protected def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
      if (request.session.get("connected").isEmpty)
        Some(Redirect(routes.AdminController.showLogin()))
      else
        None
    }
  }
}
