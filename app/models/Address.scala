package models

import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Address(hash:String,balance:Long) extends Model 
case class AddressesInfo(count: Int, balance: Long) extends Info

object Address  
{ 
  def getRichList(blockHeight: Int, table: String) = {
      val query = "select hash, ifnull(balance,0) as balance from "+table+" where hash is not null and  block_height = "+blockHeight
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Address(hashToAddress
          (row[Option[Array[Byte]]]("hash").getOrElse(Array.empty)), 
          (row[Long]("balance")/100000000).toLong
        )}).toList
      }

  }

  def getAddressesPage(hex:String) = {
      val query = "select floor(("+(pageSize-1)+" + count(*))/"+pageSize+") as c from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m "      
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Pagination(row[Int]("c"), pageSize)}).head
      }

  }

  def getAddressesInfo(hex: String) = {
      val query = "select count(*) as c, ifnull(sum(ifnull(m.balance,0)),0) as v from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m  "    
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => AddressesInfo(
          row[Int]("c"),
          row[Long]("v")
        )}).head
      }  

  }

  def getRepresentant(hex:String) = {

      val query ="SELECT hex(representant) as representant FROM addresses where hash=X'"+hex+"' union select '"+hex+"' as representant"
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => row[String]("representant")
        }).head
      }
  }

  def getAddresses(hex:String,page: Int) = {

      val query = "select m.hash as hash, m.balance as balance from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m  limit "+(page-1)*pageSize+","+pageSize
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Address(
          hashToAddress(row[Array[Byte]]("hash")),
          row[Option[Long]]("balance").getOrElse(0L)
        )}).toList
      }

  }
}