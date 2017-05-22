package controllers

import javax.inject.Inject

import models.{BoardReplyAccess, BulletinBoardAccess}
import play.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Action

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by leehwangchun on 2017. 5. 22..
  */
class ModifyController @Inject()(bc : BulletinBoardAccess, rc : BoardReplyAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends WriteModifyCommon with I18nSupport {

  def modify(id : Long) = Action.async { implicit request =>
    request.session.get("uid").map{ uid =>
      bc.getBoardContentsForModify(id, uid.toLong).map {
        case Right(a) => Ok(views.html.modify(a, writeForm))
        case Left(t) => BadRequest(t)
      }
    }.getOrElse {
      Future(BadRequest("Invalid Connection Info"))
    }
  }

  def modifyCommit = Action.async(parse.form(writeForm, onErrors = formParseError)) { implicit request =>
    val boardData = request.body
    bc.updateBoardContents(boardData.idx, boardData.contents).map {
      case Right(a) => Redirect(routes.BoardController.board())
      case Left(t) => Ok(t.toString + "Failure!!")
    }
  }
}
