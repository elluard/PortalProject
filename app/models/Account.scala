package models

import slick.driver.MySQLDriver.api._
import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.{Inject, Singleton}

import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
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

  def * = (UID, account, password, lastLoginDate, userName) <> ((Account.apply _).tupled, Account.unapply)}

@Singleton()
class AccountDataAccess @Inject()(protected val dbConfigProvider : DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  val accounts = TableQuery[Accounts]

  def insertNewUser(accountData : Account) = {
    db.run((accounts += accountData).asTry)
  }

  def verifyPassword[A, B](account : String, password : String) = {
    db.run {
      accounts
        .filter(record => record.account === account && record.password === password)
        .map(_.userName)
        .result
        .asTry
    }
  }

  def searchUserByName[A, B](account : String)(implicit ec: ExecutionContext)  = {
    db.run{
      accounts.filter(record => record.account === account).map(_.userName).result.asTry
    }
  }

  def getList = {
    db.run(accounts.filter(_.account === "elluard").map(_.userName).result.asTry)
  }
}
