package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{ Success, Failure }


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
class BulletinBoardAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider, implicit val e : ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]{
  val boardContents = TableQuery[BulletinBoards]
  val boardTitles = TableQuery[BoardTitles]
  val contentsPerPage = 10

  def getBoardTitleList(boardType : Int, page : Int) : Future[Either[String, Seq[BoardTitle]]] = {
    db.run(
      boardTitles
        .filter(a => a.boardType === 0) //where
        .sortBy(_.idx.desc)             //order by
        .drop(contentsPerPage*(page-1))     //limit
        .take(contentsPerPage)          //offset
        .result
        .asTry
    ).map {
      case Success(a) => Right(a)
      case Failure(t) => Left("Internal Server Error")
    }
  }

  def getBoardContents(id : Long) : Future[Either[String, BulletinBoard]] = {
    db.run(boardContents.filter(_.idx === id).result.headOption.asTry).map {
      case Success(a) => a.map(board => Right(board)).getOrElse(Left("No Data"))
      case Failure(t) => Left("Internal Server Error")
    }
  }

  def getBoardContentsForModify(id : Long, writerUID : Long) : Future[Either[String, BulletinBoard]] = {
    db.run(boardContents.filter(board => board.idx === id && board.writerUID === writerUID).result.headOption.asTry)
    .map {
      case Success(a) => a.map(board => Right(board)).getOrElse(Left("NoData"))
      case Failure(t) => Left("Internal Server Error")
    }
  }

  def insertBoardList(title : String, writer : String, contents : String, writerUID : Long) = {
    val boardData = BulletinBoard(0, 0, title, contents, 0, new Date(System.currentTimeMillis()), writer, writerUID)
    db.run((boardContents += boardData).asTry).map {
      case Success(a) => Right(a)
      case Failure(t) => Left(t)
    }
  }

  def updateBoardContents(id : Long, contents : String) = {
    db.run(boardContents.filter(_.idx === id).map(_.contents).update(contents).asTry).map {
      case Success(a) => Right(a)
      case Failure(t) => Left(t)
    }
  }
}

