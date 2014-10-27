package services

import java.io.File

import play.api.Play

object FileUtil {

  val FILES_DIR = "/files/"
  
  def getExtension(contentType : Option[String]) : Option[String] = {
    contentType match{
      case Some("image/png") => Some("png")
      case _ => None
    }
  }
  
  def getPDFExtension(contentType : Option[String]) : Option[String] = {
    contentType match{
      case Some("application/pdf") => Some("pdf")
      case _ => None
    }
  }
  
  def filesPath(fileName : String) : String = {
    val dir = Play.current.getFile(FILES_DIR).getAbsolutePath
    dir + "/" + fileName  
  }

  def imagePath(id : Long) : File = {
    new File(FileUtil.filesPath(id.toString + ".png" ))
  }

  def pdfPath(id : Long) : File = {
    new File(FileUtil.filesPath(id.toString + ".pdf" ))
  }

}