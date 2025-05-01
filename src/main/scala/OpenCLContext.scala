import org.jocl._

object OpenCLContext {
  def initGPU(): (cl_context, cl_command_queue) = {
    CL.setExceptionsEnabled(true)
    
    // should work on any computer with a GPU
    val numPlatforms = Array(0)
    CL.clGetPlatformIDs(0, null, numPlatforms)
    val platforms = Array.ofDim[cl_platform_id](numPlatforms(0))
    CL.clGetPlatformIDs(platforms.length, platforms, null)

    // For each platform, look for GPU devices
    val gpuDevice = platforms
      .iterator
      .flatMap { plat =>
        val nDev = Array(0)
        CL.clGetDeviceIDs(plat, CL.CL_DEVICE_TYPE_GPU, 0, null, nDev)
        if (nDev(0) > 0) {
          val devs = Array.ofDim[cl_device_id](nDev(0))
          CL.clGetDeviceIDs(plat, CL.CL_DEVICE_TYPE_GPU, nDev(0), devs, null)
          devs
        } else Seq.empty
      }
      .toSeq
      .headOption
      .getOrElse(sys.error("No GPU device found"))

    println(s"Using GPU device: $gpuDevice")

    // Create an OpenCL context & queue on that device
    val contextProps = new cl_context_properties()
    contextProps.addProperty(CL.CL_CONTEXT_PLATFORM, gpuDevice.getPlatform)
    val context = CL.clCreateContext(contextProps, 1, Array(gpuDevice), null, null, null)
    val queue = CL.clCreateCommandQueue(context, gpuDevice, 0, null)

    (context, queue)
  }

  // helper to get the platform pointer of a device
  implicit class RichDevice(dev: cl_device_id) {
    def getPlatform: cl_platform_id = {
      // Query CL_DEVICE_PLATFORM
      val buf: Array[cl_platform_id] = new Array(1)
      CL.clGetDeviceInfo(
        dev, CL.CL_DEVICE_PLATFORM, Sizeof.cl_platform_id,
        Pointer.to(buf: _*),
        null
      )
      buf(0)
    }
  }
}
