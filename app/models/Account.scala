package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by leehwangchun on 2017. 3. 25..
  */
case class Account(uid : Long, account : String, password : String, lastLoginDate : Date, userName : String)

class Accounts(tag : Tag) extends Table[Account](tag, "Accounts") {
  def UID = column[Long]("UID", O.PrimaryKey, O.AutoInc)
  def account = column[String]("Account")
  def password = column[String]("Password")
  def lastLoginDate = column[Date]("LastLoginDate")
  def userName = column[String]("UserName")

  def * = (UID, account, password, lastLoginDate, userName) <> ((Account.apply _).tupled, Account.unapply)
}

@Singleton()
class AccountDataAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider, implicit val e : ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  val accounts = TableQuery[Accounts]

  def insertNewUser(accountData : Account) = {
    db.run((accounts += accountData).asTry).map {
      // TODO : 닉네임 겹침 예외처리 해야함
      case Success(a) => Right(a)
      case Failure(t) => /*차후 로그 추가 시 t 내용 기록하도록 추가*/ Left("Internal Server Error")
    }
  }

  def verifyPassword(account : String, password : String) : Future[Either[String, Account]] = {
    db.run {
      accounts
        .filter(record => record.account === account && record.password === password)
        .result
        .headOption
        .asTry
    }.map {
      case Success(recordSet) => recordSet.map(account => Right(account)).getOrElse(Left("Invalid ID/PW"))
      case Failure(t) => /*차후 로그 추가 시 t 내용 기록하도록 추가*/ Left("Internal Server Error")
    }
  }
}
