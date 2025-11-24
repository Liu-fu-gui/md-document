#### 1.2 认证与登录

在使用 HuggingFace CLI 命令进行操作之前，需要进行认证。以下是详细的认证步骤：

##### 生成[访问令牌](https://so.csdn.net/so/search?q=访问令牌&spm=1001.2101.3001.7020)

1. 访问 [HuggingFace 官网](https://huggingface.co/)。
2. 登录你的账户。
3. 进入 `Settings` -> `Access Tokens`。
4. 点击 `New Token`，生成一个新的访问令牌。

https://huggingface.co/settings/tokens



hf_lQYexxxxxxxxxxxxxx

## huggingface使用

Getting started with our `git` and `git-lfs` interface

You can create a repository from the CLI (skip if you created a repo from the website)

```
$pip install huggingface_hub
#You already have it if you installed transformers or datasets

$huggingface-cli login
#Log in using a token from huggingface.co/settings/tokens
#Create a model or dataset repo from the CLI if needed
$huggingface-cli repo create repo_name --type {model, dataset, space}
```

Clone your model, dataset or Space locally

```
#Make sure you have git-lfs installed
#(https://git-lfs.github.com)
$git lfs install
$git clone https://huggingface.co/username/repo_name
```

Then add, commit and push any file you want, including large files

```
# save files via `.save_pretrained()` or move them here
$git add .
$git commit -m "commit from $USER"
$git push
```

In most cases, if you're using one of the [compatible libraries](https://huggingface.co/docs/hub/models-libraries), your repo will then be accessible from code, through its identifier: username/repo_name

For example for a transformers model, anyone can load it with:

```
tokenizer = AutoTokenizer.from_pretrained("username/repo_name")
model = AutoModel.from_pretrained("username/repo_name")
```