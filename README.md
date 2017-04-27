# Leitor e Conversor de arquivos .DBF
## Como usar
Para iniciar o servidor no windows:
<li>Abra o prompt de comando e mude o diretório para a pasta principal
<li>Digite <code>gradlew bootrun</code></li>
<br>
Para iniciar o servidor no linux:
<li>Abra o console e vá para o diretório principal do servidor
<li>Digite <code>./gradlew bootrun</code>

## URLS:
* _Mostrar a quantidade de registros no arquivo DBF_: <code>http://localhost:8080/getDBFRecordAmount</code>

* _Mostrar o conteudo de um arquivo SQLite_: <code>http://localhost:8080/showSQLiteDB?sqlitepath=/caminho_do_arquivo/exemplo.sqlite&start=X&amount=Y</code>
	* **sqlitepath**: Caminho para o arquivo do BD;
	* **page**: Número da página;
	* **amount**: Número de registros por página.

* _Conteudo do arquivo SQLite em formato_ JSON: <code>http://localhost:8080/openSQLiteDB?path=/caminho_do_arquivo/exemplo.sqlite&start=X&amount=Y</code>
	* **path**: Caminho para o arquivo do BD;
	* **start**: O registro inicial da página;
	* **amount**: A quantidade de registros por página.

* _Conteudo do arquivo DBF em formato_ JSON: <code>http://localhost:8080/openDBF?path=/caminho_do_arquivo/exemplo.dbf&page=X&amountPerPage=Y</code>
	* **path**: Caminho para o arquivo DBF;
	* **page**: Página a ser mostrada;
	* **amountPerPage**: Quantidade de registros por página.

* _Conteúdo do arquivo SQLite em formato_ JSON _com a ordenação na coluna desejada_: <code>http://localhost:8080/openSQLiteSortedColumn?<strong>path</strong>=/caminho_do_arquivo/exemplo.sqlite&<strong>start</strong>=0&<strong>sortBy</strong>=1&<strong>name</strong>=NOME_DA_TABELA</code>
	* **path**: Caminho para o banco de dados;
	* **start**: Registro inicial da página;
	* **sortBy**: 0 para mostrar em ordem decrescente e 1 para ordem crescente;
	* **name**: Nome da tabela.

* _Converter um arquivo DBF para_ SQLite: <code>http://localhost:8080/dbfToSqlite?dbfpath=/caminho_do_arquivo_dbf/exemplo.dbf&sqlitepath=/caminho_do_arquivo_sqlite/exemplo.sqlite</code>
	* **dbfpath**: Caminho para o arquivo DBF;
	* **sqlitepath**: Caminho para o arquivo do sqlite.



## DBFreader:
<li><code>npm install && npm run</code>
<li><code>npm start</code>
