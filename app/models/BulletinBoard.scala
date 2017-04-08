package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile


/**
  * Created by leehwangchun on 2017. 4. 8..
  */

case class BulletinBoard(idx : Long, boardType : Int, title : String, contents : String, hitCount : Int, writeDate : Date)

class BulletinBoards(tag : Tag) extends Table[BulletinBoard](tag, "bulletinBoard"){
  def idx = column[Long]("idx", O.PrimaryKey, O.AutoInc)
  def boardType = column[Int]("boardType")
  def title = column[String]("title")
  def contents = column[String]("contents")
  def hitCount = column[Int]("hitCount")
  def writeDate = column[Date]("writeDate")

  def * = (idx, boardType, title, contents, hitCount, writeDate) <> ((BulletinBoard.apply _).tupled, BulletinBoard.unapply)
}

@Singleton()
class BulletinBoardAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]{
  val boardContents = TableQuery[BulletinBoards]

  def getBoardList(boardType : Int) = {
    db.run(boardContents.filter(a => a.boardType === 0).result.asTry)
  }

  def insertBoardList(contents : BulletinBoard) = {
    db.run((boardContents += contents).asTry)
  }
}

