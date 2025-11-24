### 4. **编译 GCC**

作为最后的手段，你可以手动编译并安装 GCC。这将不依赖于 SCL 或任何外部工具包。参考以下步骤：

1. 下载 GCC 源代码（例如 GCC 8.5.0）：

   ```
   bash复制代码wget http://ftp.gnu.org/gnu/gcc/gcc-8.5.0/gcc-8.5.0.tar.gz
   tar -xvzf gcc-8.5.0.tar.gz
   cd gcc-8.5.0
   ```

2. 安装构建所需的依赖：

   ```
   bash
   
   
   复制代码
   sudo yum install -y gcc-c++ gmp-devel mpfr-devel libmpc-devel
   ```

3. 配置并编译 GCC：

   ```
   bash复制代码./contrib/download_prerequisites
   mkdir build
   cd build
   ../configure --enable-languages=c,c++ --disable-multilib
   make -j$(nproc)
   sudo make install
   ```

这些步骤将手动编译 GCC，而不依赖于 `devtoolset`。完成后，你可以通过 `gcc --version` 来验证安装的 GCC 版本。