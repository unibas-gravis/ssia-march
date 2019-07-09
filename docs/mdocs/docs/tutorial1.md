{% include head.html %}

# Hello Scalismo!

The goal in this tutorial is to present the most important data structures, as well as the visualization capabilities of Scalismo.

## Initializing the system

Before we start, we need to initialize Scalismo by calling:

```scala mdoc:silent
scalismo.initialize()
implicit val rng = scalismo.utils.Random(42)
```

The call to ```scalismo.initialize``` loads all the dependencies to native C++ libraries (such as e.g. [vtk](https://www.vtk.org) or [hdf5](https://www.hdf-group.org)). 
The second call tells scalismo, which source
of randomness to use and at the same time seeds the random number generator appropriately.

Later on we would like to visualize the objects we create. This is done using [Scalismo-ui](https://github.com/unibas-gravis/scalismo-ui) - the visualization library accompanying scalismo. 
We can load an instance of the GUI, which we name here simply ```ui``` as follows:

```scala mdoc:silent
import scalismo.ui.api.ScalismoUI

val ui = ScalismoUI()
```


## Meshes (surface data)

The first fundamental data structure we discuss is the triangle mesh, which is defined in the package ```scalismo.mesh```.

In the following we will need access to the following object, which we now import:

```scala mdoc:silent
import scalismo.mesh.TriangleMesh // the mesh class
import scalismo.io.MeshIO // to read meshes
import scalismo.common.PointId // to refer to points by id
import scalismo.mesh.TriangleId // to refer to triangles by id
import scalismo.geometry._3D // indicates that we work in 3D space
```

Meshes can be read from a file using the method ```readMesh``` from the ```MeshIO```:
```scala mdoc:silent
val mesh : TriangleMesh[_3D] = MeshIO.readMesh(new java.io.File("datasets/1.stl")).get
```
To visualize any object in Scalismo, we can use the ```show``` method of the ```ui``` object. 
We often want to organize different visualizations of an object in a group. 
We start directly with this practice and  first create a new group, to which we then add the visualization of the mesh:

```scala mdoc:silent
val group = ui.createGroup("femur")
val meshView = ui.show(group, mesh, "femur mesh")
```

Now that the mesh is displayed in the "Scalismo Viewer's 3D view", you can interact with it as follows: 

* to rotate: maintain the left mouse button clicked and drag  
* to shift/translate: maintain the middle mouse button clicked and drag
* to scale: maintain the right mouse button clicked and drag up or down

*Note: if you are a Mac user, please find out how to emulate these events using your mouse or trackpad*

*Note also that you can use the *RC*, *X*, *Y* and *Z* buttons in the 3D view to recenter the camera on the displayed object.*

#### Anatomy of a Triangle mesh
A 3D triangle mesh in scalismo consists of a ```pointSet```, which maintains a collection of 3D points and a 
list of triangle cells. We can access individual points using their point id. 

Here we show how we can access the first point in the mesh:

```scala mdoc
println("first point " + mesh.pointSet.point(PointId(0)))
```

Similarly, we can access the first triangles as follows:

```scala mdoc
println("first cell " + mesh.triangulation.triangle(TriangleId(0)))
```

The first cell is a triangle between the first, second and third points of the mesh.
Notice here that the cell indicates the identifiers of the points (their index in the point sequence)
instead of the geometric position of the points.

Instead of visualizing the mesh, we can also display the points forming the mesh. 

```scala mdoc:silent
val pointCloudView = ui.show(group, mesh.pointSet, "pointCloud")
```

This should add a new point cloud element to the scene with the name "pointCloud".

Note that to clean up the 3D scene, you can delete the objects either from the user interface (by right-clicking on the object's name), or programmatically by calling ```remove``` on the corresponding view object :

```scala mdoc:silent
pointCloudView.remove()
```

## Scalar Images

The next important data structure is the (scalar-) image.

A *discrete* scalar image (e.g. gray level image) in Scalismo is simply a function from a discrete domain of points to a scalar value. 

We will need the following imports:
```scala mdoc:silent
import scalismo.io.ImageIO; // to read images
import scalismo.image.{DiscreteScalarImage, DiscreteScalarImage3D} // discrete images
import scalismo.image.ScalarImage // continuous images
import scalismo.geometry.{IntVector, IntVector3D} // represent image indices
```

Let's read and display a 3D image (MRI of a human):

```scala mdoc:silent
val image = ImageIO.read3DScalarImage[Short](new java.io.File("datasets/1.nii")).get
val imageView = ui.show(group, image, "CT")
```

*Note: depending on your view on the scene, it could appear as if the image is not displayed. In this case, make sure to rotate the scene and change the position of the slices as indicated below.*

To visualize the different image slices in the viewer, select "Scene" (the upper node in the scene tree graph) and use the X,Y,Z sliders.

You can also change the way of visualizing the 3D scene under the

*View -> Perspective* menu.


### operations images

Given that discrete scalar images are a mapping between points and values, we can easily create such images programmatically.

Here we create a new image defined on the same domain of points with artificially created values:

 We threshold the image, where all the values below 200 are replaced with 0 and those above with 1.

```scala mdoc:silent
val thresholdedImage : DiscreteScalarImage[_3D,Short] = image.map(v => if (v <= 200) 0 else 1)
ui show(group, thresholdedImage, "thresh")
```


## Statistical Mesh Models

Finally, we look at Statistical Shape Models. 

We need the following imports

```scala mdoc:silent
import scalismo.io.StatisticalModelIO // to read statistical shape models
import scalismo.statisticalmodel.StatisticalMeshModel // the statistical shape models
```

Statistical models can be read by calling ```readStatisticalMeshModel``` 
```scala mdoc:silent
val shapeModel = StatisticalModelIO.readStatisticalMeshModel(new java.io.File("datasets/pcamodel.h5")).get
val modelGroup = ui.createGroup("model")
val faceModelView = ui.show(modelGroup, shapeModel, "shape model")
```

### Sampling in the UI

*Exercise: Sample random instances of shapes by using the graphical tools in the scene pane : click on the "model" tree node and then the "Random" button*

*Exercise: click a landmark on a recognizable position of the shaoe, (use the toggle crosshair button in the toolbar to activate landmark clicking). Now continue sampling from the model. What happens to the selected point?*

As you can see, a new instance of the shape model is displayed each time along with the corresponding landmark point. Notice how the position of the landmark point changes in space while it keeps the same "meaning" on the shape.


