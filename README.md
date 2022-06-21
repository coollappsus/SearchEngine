<h1 align="center">SearchEngine</h1>

<div align="center">
    <img src="https://img.shields.io/badge/Java-black?style=for-the-badge&logo=Java" alt="Java"/>
    <img src="https://img.shields.io/badge/Spring-black?style=for-the-badge&logo=Spring" alt="Spring"/>
    <img src="https://img.shields.io/badge/Mysql-black?style=for-the-badge&logo=Mysql" alt="MySQL"/>
</div>
<h1></h1>

<h1>Description</h1>
This project was created for educational purposes to obtain a diploma on the Skillbox educational platform.

<h1>General info</h1>
<!--Нарисовать структуру MVC-->
<!--Что необходимо иметь для запуска и как запустить-->

<h1>About the project</h1>
<!--Описать функционал подробно со скринами -->
<!--Возможно приложить структуру БД-->
The functionality of the program can be divided into three parts:
<ul>
    <li>View general statistics about all sites and about each site individually</li>
    <li>Start or stop full indexing, indexing of a single page in the sites that are in the config file</li>
    <li>Search by search query on all sites or on a selected site</li>
</ul>
<h3>Dashboard</h3>
This tab opens by default. It displays general statistics for all sites, as well as detailed statistics and status
of the sites.
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/Dashboard2.png?raw=true" alt="Dashboard"></div>
<div>
    General statistics include:
    <ul>
        <li>The sites counter</li>
        <li>The pages counter</li>
        <li>The lemmas counter</li>
    </ul>
</div>
<div>
    Detailed statistics include:
    <ul>
        <li>Status time - the time of the last change in the status of the site</li>
        <li>Pages - the pages counter of the site</li>
        <li>Lemmas - the lemmas counter of the site</li>
        <li>Error - indexing error if there was one</li>
    </ul>
</div>
<h3>Management</h3>
This tab contains the search engine management tools — starting and stopping full indexing (reindexing),
as well as the ability to add (update) a separate page by link. Indexing of sites is performed independently
in different threads.
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/Management.png?raw=true"></div>
<h3>Search</h3>
This page is designed to perform a search on indexed sites of the search engine. 
It contains a search field, a drop-down list with a choice of the site to search for, 
and when you click on the "Find" button, the search results are displayed.
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/SearchEmpty.png?raw=true"></div>
<h3></h3>
<div>No matter what form the word is in the search query, the search engine will still find it.
    All links are active (at the time of indexing) and tabs open in a new window. Search queries are displayed in
    descending order of relevance. The relevance of the page is calculated during the search process.</div>
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/Search1.png?raw=true"></div>
<h3></h3>
<div>If the search words are in the same sentence, the search engine will combine them into one snippet.</div>
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/SearchOneResult.png?raw=true"></div>
<h3></h3>
<div>If there are more than ten search results, they are displayed in parts (ten results each),
    and the corresponding "Show more" button is displayed at the end of the page.</div>
<div><img src="https://github.com/coollappsus/SearchEngine/blob/main/assets/SearchShowMore.png?raw=true"></div>

<h1>Technologies</h1>
<!--Расписать какие использовались зависимости с версиями-->
<ul>
    <li>Java - version 17</li>
    <li>Spring boot - version 2.6.7</li>
    <li>MySQL - version 8.0.26</li>
</ul>
<h1>Code Examples</h1>
<!--Сделать скрины, что выводят контроллеры при тех или иных запросах и и ответах-->

<h1>Features</h1>
<!--Написать особенности, мейби надо удалить-->

<h1>Status</h1>
<!--прописать статус-->

<h1>Inspiration</h1>
The project was created for educational purposes.

<h1>Future scope</h1>
<!--Здесь написать, что можно было бы добавить в дальнейшем, посмотреть доп.задачи в ТЗ-->

<h1>Contact</h1>
Created by <a href="https://t.me/coollappsus"> Ryzhikov Ivan</a> - feel free to contact me!
<h3></h3>

<div><b>+7 (952) 353-19-01</b> | <a href="mailto:ntdr.94@yandex.ru"> Send me mail</a>  |
    <a href="https://github.com/coollappsus"> github</a></div>