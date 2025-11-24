# git记住账号密码
## 记住账号密码
进入项目目录

```
git config --global credential.helper store
```
然后会生成一个本地文件用于记录用户名和密码，这个文件我们无需关心
再次git pull一下，会让输入用户名和密码。这次输入之后以后就不会每次输入了。

## 清除用户名和密码
运行一下命令缓存输入的用户名和密码

- Windows主机

```
git config --global credential.helper wincred
```
- mac
- 
```
git config --global credential.helper osxkeychain
```