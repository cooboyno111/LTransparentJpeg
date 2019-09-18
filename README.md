# LTransparentJpeg

这是研究图像压缩的衍生结果

Jpeg有这比png更高的压缩率，但其不能支持透明度。

本工具在Jpeg的尾部直接添加了透明区块。

此工具的理论基础 https://www.cnblogs.com/U-tansuo/p/JPG-Mask.html

实现了PNG24/32更高压缩率的图片压缩方法，其压缩结果与最优秀的png8压缩工具结果类似。

需要专门的程序读取工具。

需要安装ImageMagick因为此程序需要用convert

sudo apt-get install imagemagick

chmod +x guetzlijpg
