package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile


/**
  * Created by leehwangchun on 2017. 4. 8..
  */

case class BulletinBoard(idx : Long, boardType : Int, title : String, contents : String, hitCount : Int, writeDate : Date, writerName : String)

class BulletinBoards(tag : Tag) extends Table[BulletinBoard](tag, "bulletinBoard"){
  def idx = column[Long]("idx", O.PrimaryKey, O.AutoInc)
  def boardType = column[Int]("boardType")
  def title = column[String]("title")
  def contents = column[String]("contents")
  def hitCount = column[Int]("hitCount")
  def writeDate = column[Date]("writeDate")
  def writerName = column[String]("writerName")

  def * = (idx, boardType, title, contents, hitCount, writeDate, writerName) <> ((BulletinBoard.apply _).tupled, BulletinBoard.unapply)
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
    db.run(boardTitles.filter(a => a.boardType === 0).result.asTry)
  }

  def getBoardContents(id : Long) = {
    db.run(boardContents.filter(_.idx === id).result.headOption)
  }

  def insertBoardList(contents : BulletinBoard) = {
    db.run((boardContents += contents).asTry)
  }
}

