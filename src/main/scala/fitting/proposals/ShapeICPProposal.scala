package fitting.proposals

import fitting.parameters.Sample
import scalismo.common.PointWithId
import scalismo.geometry._3D
import scalismo.mesh.TriangleMesh
import scalismo.numerics.UniformMeshSampler3D
import scalismo.sampling.{ProposalGenerator, TransitionProbability}
import scalismo.statisticalmodel.{MultivariateNormalDistribution, StatisticalMeshModel}

/**
  * Created by luetma00 on 21.10.16.
  */
case class ShapeICPProposal(model: StatisticalMeshModel,
                            targetMesh: TriangleMesh[_3D],
                            targetUncertainty : MultivariateNormalDistribution,
                            stepLength: Double,
                            noiseVariance : Double =25)(implicit val rng : scalismo.utils.Random) extends
  ProposalGenerator[Sample] with TransitionProbability[Sample] {

  val targetPoints = UniformMeshSampler3D(targetMesh, 2000).sample().map(_._1)
  val referenceMesh = model.referenceMesh
  val gpInterpolated = model.gp.interpolateNearestNeighbor


  override def propose(theta: Sample): Sample = {
    val inversePoseTransform = theta.poseTransformation.inverse

    val currentMesh = model.instance(theta.parameters.modelCoefficients)
    val targetsInModelSpace = targetPoints.map(inversePoseTransform)
    val td = for (targetPointModelSpace <- targetsInModelSpace) yield {
      val refId = currentMesh.pointSet.findClosestPoint(targetPointModelSpace).id
      val refPt = model.referenceMesh.pointSet.point(refId)
      (refPt, targetPointModelSpace - refPt)
    }

      val icpCoeffs = gpInterpolated.coefficients(td.toIndexedSeq, sigma2=noiseVariance)
      val currentModelParameters = theta.parameters.modelCoefficients
      val newShapeParameters = currentModelParameters + (icpCoeffs - currentModelParameters) * stepLength

    val newParameters = theta.copy(
      parameters = theta.parameters.copy(modelCoefficients = newShapeParameters),
      generatedBy = "shapeicp"
    )
    newParameters
  }

  override def logTransitionProbability(from: Sample, to: Sample) = {
    0.0
  }
}