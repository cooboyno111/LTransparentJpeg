# LTransparentJpeg

研究图像极限压缩的衍生结果

Jpeg格式有着比png更高的压缩率，但其不能支持透明度。

本工具在Jpeg的尾部直接添加了xz压缩的透明区块。

此工具的理论基础 https://www.cnblogs.com/U-tansuo/p/JPG-Mask.html

实现了PNG24/32更高压缩率的图片压缩方法，其压缩结果与最优秀的png8压缩工具结果类似。

实现了PNG 442KB-》62KB的压缩结果。

需要专门的程序读取工具，见ltjpegToPng方法。

采用此种方式后处理图片加上专用的unity加载工具估计能让包体积降低一半以上。

需要安装ImageMagick因为此程序需要用convert

sudo apt-get install imagemagick

chmod +x guetzlijpg
