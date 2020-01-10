Gitblit 使用 black 命令检查 Python 编码规范的插件
========================================

安装和配置
------------

    mvn clean package && cp target/*.zip /path/to/gitblit/data/plugins


修改配置文件：data/gitblit.properties
pycheck.repositories = project1.git,project2.git

License
-------

The script is released under the MIT License.  The MIT License is registered
with and approved by the Open Source Initiative [1]_.

.. [1] https://opensource.org/licenses/MIT
