package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile


/**
  * Created by leehwangchun on 2017. 4. 8..
  */

case class BulletinBoard(idx : Long, boardType : Int, title : String, contents : String, hitCount : Int, writeDate : Date, writerName : String, writerUID : Long)

class BulletinBoards(tag : Tag) extends Table[BulletinBoard](tag, "bulletinBoard"){
  def idx = column[Long]("idx", O.PrimaryKey, O.AutoInc)
  def boardType = column[Int]("boardType")
  def title = column[String]("title")
  def contents = column[String]("contents")
  def hitCount = column[Int]("hitCount")
  def writeDate = column[Date]("writeDate")
  def writerName = column[String]("writerName")
  def writerUID = column[Long]("writerUID")

  def * = (idx, boardType, title, contents, hitCount, writeDate, writerName, writerUID) <> ((BulletinBoard.apply _).tupled, BulletinBoard.unapply)
}

case class BoardTitle(idx : Long, boardType : Int, title : String, hitCount : Int, writeDate : Date, writerName : String)

class BoardTitles(tag : Tag) extends Table[BoardTitle](tag, "bulletinBoard"){
  def idx = column[Long]("idx", O.PrimaryKey, O.AutoInc)
  def boardType = column[Int]("boardType")
  def title = column[String]("title")
  def hitCount = column[Int]("hitCount")
  def writeDate = column[Date]("writeDate")
  def writerName = column[String]("writerName")

  def * = (idx, boardType, title, hitCount, writeDate, writerName) <> ((BoardTitle.apply _).tupled, BoardTitle.unapply)
}

@Singleton()
class BulletinBoardAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]{
  val boardContents = TableQuery[BulletinBoards]
  val boardTitles = TableQuery[BoardTitles]

  def getBoardTitleList(boardType : Int) = {
    db.run(boardTitles.filter(a => a.boardType === 0).sortBy(_.idx.desc).result.asTry)
  }

  def getBoardContents(id : Long) = {
    db.run(boardContents.filter(_.idx === id).result.headOption)
  }

  def insertBoardList(title : String, writer : String, contents : String, writerUID : Long) = {
    val boardData = BulletinBoard(0, 0, title, contents, 0, new Date(System.currentTimeMillis()), writer, writerUID)
    db.run((boardContents += boardData).asTry)
  }
}

