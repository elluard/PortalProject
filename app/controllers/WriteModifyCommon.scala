package controllers

import play.api.data.Forms._
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc.{Controller, Result}

/**
  * Created by leehwangchun on 2017. 5. 22..
  */
case class WriteForm(idx : Long, title : String, writer : String, contents : String)

trait WriteModifyCommon extends Controller{
  val writeForm = Form(
    mapping(
      "idx" -> longNumber,
      "title" -> nonEmptyText,
      "writer" -> nonEmptyText,
      "contents" -> nonEmptyText
    )(WriteForm.apply)(WriteForm.unapply)
  )

  def formParseError(formWithErrors: Form[WriteForm]): Result = {
    BadRequest("form parse error" + formWithErrors.toString)
  }
}
