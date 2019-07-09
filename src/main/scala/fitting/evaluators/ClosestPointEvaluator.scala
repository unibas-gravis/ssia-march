package fitting.evaluators

import fitting.parameters.Sample
import scalismo.common.{DiscreteField, Field, PointId, RealSpace, UnstructuredPointsDomain}
import scalismo.common.interpolation.FieldInterpolator
import scalismo.geometry.{EuclideanVector, Point, _3D}
import scalismo.mesh.boundingSpheres.{ClosestPointInTriangle, ClosestPointIsVertex, ClosestPointOnLine}
import scalismo.mesh.{SurfacePointProperty, TriangleMesh}
import scalismo.numerics.UniformMeshSampler3D
import scalismo.sampling.DistributionEvaluator
import scalismo.statisticalmodel.{MultivariateNormalDistribution, StatisticalMeshModel}
import scalismo.utils.MeshConversion
import utils.DecimateModel




case class ClosestPointEvaluator(model: StatisticalMeshModel,
                                 target : TriangleMesh[_3D],
                                 uncertainty: MultivariateNormalDistribution
                                )(implicit val rng : scalismo.utils.Random)
  extends DistributionEvaluator[Sample] {

  val targetPoints = UniformMeshSampler3D(target, 2000).sample().map(_._1)

  val modelDec = DecimateModel.decimate(model, 0.2)

  override def logValue(sample: Sample): Double = {

    val currModelInstance = modelDec.instance(sample.parameters.modelCoefficients).transform(sample.poseTransformation)

    val likelihoods = targetPoints.map( targetPoint => {
      val closestPointOnModel = currModelInstance
        .pointSet.findClosestPoint(targetPoint).point
        //.operations.closestPointOnSurface(targetPoint).point
      val observedDeformation = targetPoint - closestPointOnModel

      uncertainty.logpdf(observedDeformation.toBreezeVector)
    })


    val loglikelihood = likelihoods.sum
    loglikelihood
  }


}