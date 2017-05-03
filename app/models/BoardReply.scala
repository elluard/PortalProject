package models


import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import javax.inject.{Inject, Singleton}


/**
  * Created by leehwangchun on 2017. 5. 1..
  */

case class BoardReply(idx : Long, boardContentID : Long, writerUID : Long, writerName : String, replyContent : String)

class BoardReplies(tag : Tag) extends Table[BoardReply](tag, "boardReply"){
  def idx = column[Long]("idx", O.AutoInc, O.PrimaryKey)
  def boardContentID = column[Long]("boardContentID")
  def writerUID = column[Long]("writerUID")
  def writerName = column[String]("writerName")
  def replyContent = column[String]("replyContent")

  def * = (idx, boardContentID, writerUID, writerName, replyContent) <> ((BoardReply.apply _).tupled, BoardReply.unapply)
}

@Singleton
class BoardReplyAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]{
  val replies = TableQuery[BoardReplies]

  def getReplies(boardContentID : Long) = {
    db.run(replies.filter(_.boardContentID === boardContentID).result)
  }

  def insertReply(boardContentID : Long, writerUID : Long, writerName : String, replyContent : String) = {
    val replyData = BoardReply(0, boardContentID, writerUID, writerName, replyContent)
    db.run((replies += replyData).asTry)
  }
}


