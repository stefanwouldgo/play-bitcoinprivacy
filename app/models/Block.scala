package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

abstract class BlockData
case class Block(hash: String, height: Int, tx: Long, value: Long) extends BlockData

object Block{
   def getBlocks(number: Int, blockHeight: Int) = Future {
    val blocksMin = blockHeight - number
    
      DB.withConnection { implicit connection =>
        (SQL("select hex(b.hash) as hash, b.block_height as block_height, ifnull(m.y,0) as tx_count, ifnull(m.z, 0) as btc from blocks b left join " +
          " (select block_height as x, count(distinct(transaction_hash)) as y, sum(value) as z from movements where block_height > " + blocksMin + " group by block_height) m " +
          " on b.block_height = m.x where b.block_height > " + blocksMin + " order by block_height desc" )() map {
          row => Block(
            row[String]("hash"),
            row[Int]("block_height"),
            row[Int]("tx_count"),
            row[Long]("btc")
          )}).toList
      }
    
   }

  def getBlockHeight = Future {
      DB.withConnection{ implicit connection =>
        SQL("select max(block_height) as c from blocks")().head[Int]("c")
      }
    
  }

}