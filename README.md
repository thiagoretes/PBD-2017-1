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
<li>Mostrar o conteudo de um arquivo SQLite: <code>http://localhost:8080/showSQLiteDB?sqlitepath=/caminho_do_arquivo/exemplo.sqlite</code>
<li>Conteudo do arquivo SQLite em formato JSON: <code>http://localhost:8080/openSQLiteDB?path=/caminho_do_arquivo/exemplo.sqlite</code>
<li>Conteudo do arquivo DBF em formato JSON: <code>http://localhost:8080/openDBF?path=/caminho_do_arquivo/exemplo.dbf</code>
<li>Converter um arquivo DBF para SQLite: <code>http://localhost:8080/dbfToSqlite?dbfpath=/caminho_do_arquivo_dbf/exemplo.dbf&sqlitepath=/caminho_do_arquivo_sqlite/exemplo.sqlite</code> 

