package fitting.parameters

import breeze.linalg.DenseVector
import scalismo.geometry.{EuclideanVector, Point, _3D}
import scalismo.registration.{RigidTransformation, RotationTransform, TranslationTransform}

case class Parameters(translationParameters: EuclideanVector[_3D],
                      rotationParameters: (Double, Double, Double),
                      modelCoefficients: DenseVector[Double])



case class Sample(generatedBy : String, parameters : Parameters, rotationCenter: Point[_3D]) {

  def poseTransformation : RigidTransformation[_3D] = {

    val translation = TranslationTransform(parameters.translationParameters)
    val rotation = RotationTransform(
      parameters.rotationParameters._1,
      parameters.rotationParameters._2,
      parameters.rotationParameters._3,
      rotationCenter
    )
    RigidTransformation(translation, rotation)
  }

}