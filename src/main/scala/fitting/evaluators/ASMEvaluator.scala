package fitting.evaluators

import fitting.parameters.Sample
import scalismo.geometry._3D
import scalismo.image.DiscreteScalarImage
import scalismo.mesh.TriangleMesh
import scalismo.sampling.{DistributionEvaluator, TransitionProbability}
import scalismo.statisticalmodel.MultivariateNormalDistribution
import scalismo.statisticalmodel.asm.{ActiveShapeModel, PreprocessedImage}

import scala.util.Random

class ASMEvaluator(asm: ActiveShapeModel, image: DiscreteScalarImage[_3D, Short]) extends DistributionEvaluator[Sample] {


  val preprocessedImage = asm.preprocessor(image.map(_.toFloat))


  val ids = Random.shuffle(asm.profiles.ids)


  def likelihoodForMesh(asm: ActiveShapeModel,
                        mesh: TriangleMesh[_3D],
                        preprocessedImage: PreprocessedImage): Double = {


    val likelihoods = for (id <- ids.par) yield {

      val profile = asm.profiles(id)
      val profilePointOnMesh = mesh.pointSet.point(profile.pointId)

      val pointToTest = profilePointOnMesh
      val featureAtPoint = asm.featureExtractor(preprocessedImage, pointToTest, mesh, profile.pointId).get


      profile.distribution.logpdf(featureAtPoint)
    }
    likelihoods.sum
  }

  override def logValue(sample: Sample): Double = {
    val currModelInstance = asm.statisticalModel.instance(sample.parameters.modelCoefficients).transform(sample.poseTransformation)
    likelihoodForMesh(asm, currModelInstance, preprocessedImage)
  }

}
