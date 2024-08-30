Raft-Java部署与debug

	1.Docker（选择docker linux容器作为开发和部署平台）
		选择最新版本ubuntu作为镜像，绑定挂载本地目录, 便于在本地机器开发，容器环境内部署。
			docker pull ubuntu:latest:
				"docker run -it --mount type=bind,source=/Users/wangchanghao/SelfStudy/RaftProject,target=/mnt/RaftProject --name linux ubuntu:latest"	
		

	2.example delopy失败
		-maven依赖还是java1.7，需要手动改成1.8
		-老版本rockdb不支持ARM64，更改至7.0.4版本解决


	3.部署成功，写测试失败。
		-原因定位到是ExampleProto类初始化失败，查看log发现是protobuf问题，导致节点通信的序列化和反序列化错误


	4.解决protobuf的问题

		4.1尝试更换protobuf 3.1
			-失败，因为源码proto方法文件语法是protobuf 2.0

		4.2尝试在容器里下载protobuf 2.50重新生成proto类
			-失败，编译阶段失败，因为protobuf 2.50不支持ARM 64架构，有解决方法需要打补丁，考虑到后续amd64开发环境遂放弃

		4.3更换linux镜像，换成了x86-64版本ubuntu，重新拉容器绑定挂载
			-成功，但编译阶段遇到问题，dependency-tracking失败，为了保证项目的健壮，不选择disable-dependency-tracking（强行编译）。
			-查看config.log，发现是环境问题，缺这三个依赖zlib1g-dev make libpthread-stubs0-dev，安装后重新编译，install，成功。

	5.重新部署，测试读写
		成功

Raft-java源码精读
	1.状态机



Raft-java项目拓展1:接入rocksdb		


Raft-java项目拓展2:引入Springboot框架
		
