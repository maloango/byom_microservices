﻿<a name="top"></a>
<br><br>Документы - это страницы с информацией, размещаемой в разделах электронных кабинетов участников Системы. Вы можете выбирать документы из списка.
Есть два типа документов: &quot;Статические&quot; и &quot;Динамические
документы&quot;. Статические документы - это только файлы в формате pdf, размещаемые отдельному участнику или группе. Динамические документы - это документы формата html,
размещаемые для одной или более групп пользователей. Эти документы могут принимать индивидуальный вид для участников, так как информация в них зависит от данных в полях профиля участника, который открывает страницу. 
<span class="admin">
Типичный дополнительный документ - это договор ссуды или любой вид заявки, который участник может использовать, чтобы запросить что-нибудь от администрации.<br>
Динамический документ может быть показан непосредственно, когда участник открывает его;
а также можно сначала запросить для заполнения форму, поля которой нужно заполнить участнику. Когда участник
отправляем заполненную форму, то полученный документ может включать указанные в ней данные, а также поля профиля участника.

<br><br><i>Где это найти?</i><br>
Документы размещены в разделе &quot;Меню: Управление содержанием >
Документы&quot;. Пример создания динамического документа может есть на сайте проекта приложения wiki, раздел  &quot;установки - дополнительные документы&quot;.
<br>
Доступ к существующим индивидуальным документам участника регулируется в  <a
	href="/do/member/manual?page=profiles"><u> странице личных данных</u></a> участника (раздел 
&quot;данные участника&quot;).

<br><br><i>Как это работает?</i><br>
Прежде чем создавать документы, нужно вначале установить <a href="/do/member/manual?page=
groups#manage_group_permissions_admin_member"><u>разрешения</u></a>. Это делается в разделе &quot;Документы&quot; через несколько опций. 
После установления разрешений вы сможете создавать новые документы через &quot;Меню:
Управление содержимым > Документы&quot;.<br>
<br><br>По каждому создаваемому документу его доступность устанавливается в 
<a href="/do/member/manual?page=groups#manage_group_permissions_member"><u>разрешениях</u></a> группы, блок
&quot;документы&quot;. Это означает, что доступ к документам устанавливается на конкретные группы  участников. Можно открывать доступ к документам только для администраторов, только для 
администраторов и брокеров, и для администраторов, брокеров и участников лично
(участники никогда не могут просматривать документы других участников).<br>
<b>Примечание:</b> В системе нет документов админа - только документы участника. 
</span>
<span class="member">
<br><br><i>Где это найти?</i><br>
При наличии соответствующих разрешений, вы можете просмотреть документы в &quot;Меню: Моя почта >
Документы&quot;.
</span>
<hr>

<span class="admin"> <a name="document_list"></a>
<h3>Список дополнительных документов</h3>
Страницы содержат список с дополнительными  <a href="#top"><u>документами</u></a>, которые были
созданы в системе. Наряду с названием документа список 
список показывает следующие данные:
<ul>
	<li><b>тип:</b> это <a href="#top"><u>тип</u></a>
	документа.
	<li><img border="0" src="/pages/images/edit.gif" width="16" height="16">&nbsp;
	нажмите значок редактирования документа, чтобы изменить его.
	<li><img border="0" src="/pages/images/view.gif" width="16" height="16">&nbsp;
	нажмите значок просмотра, чтобы просмотреть результат.
	<li><img border="0" src="/pages/images/delete.gif" width="16" height="16">&nbsp;
	нажмите значок удаления, чтобы удалить документ.
</ul>
Чтобы создать новый документ, вы должны видеть одну из двух кнопок в нижней части окна
(&quot;новый динамический документ&quot; или &quot;новый статический документ&quot;)
<hr class="help">
</span>

<span class="admin"> <a name="new_edit_static_document"></a>
<h3>Добавить/Изменить новый статический документ</h3>
Это позволяет вам добавить <a href="#top"><u>новый статический
документ</u></a>. Форма очень простая: просто введите название и описание документа, 
заполните имя файла в поле &quot;Загрузить файл&quot;. Для этого 
используйте кнопку &quot;Выбрать&quot;.<br>
После завершения нажмите кнопку &quot;выполнить&quot;, чтобы сохранить файл.
<br><br>Файл документа может быть в любом формате. Если вы выбрали "изменить
существующий файл", этот файл размещен по ссылке &quot;Используемый файл 
&quot;; вы можете нажать на ссылку, чтобы просмотреть текущую версию документа.
<br><br><b>Внимание</b>: Одно только создание документа не означает, что ваши
участники/пользователи смогут его просмотреть. После создания документа, вы должны установить <a
	href="/do/member/manual?page=groups#manage_group_permissions_member"><u>разрешения  
участников</u></a> по просмотру документов, выбирая новый документ
в раскрывающемся меню в разделе прав доступа к документам.
<hr class="help">
</span>


<span class="admin"> <a name="new_edit_dynamic_document"></a>
<h3>Добавить/изменить новый динамический документ</h3>
Позволяет вам добавить <a href="#top"><u>новый динамический документ</u></a>. В форме есть следующие поля:
<ul>
	<li><b>Название:</b> Название документа.
	<li><b>Описание:</b> Описание документа (только для
	административных пользователей)
	<li><b>Формуляр:</b> Возможно, что перед выведением, в документ вначале необходимо ввести некоторые данные. На этой странице вы можете написать html страницу
    с формой запроса ввода пользователем необходимых данных. Если вам не нужны эти
   функции, то вы можете оставить ее незаполненной.
	<li><b>Страница документа:</b> Здесь вы можете написать страницу документа в html формате. Если вы определили страницу формы в вышеуказанном поле, 
	тогда вы можете включать введенные пользователем данные с этой страницы. 	Страница документа откроется во всплывающем окне с кнопкой "печать" и кнопкой
    "закрыть документ". Вы можете также вставить изображения. Вам необходимо будет сначала загрузить
	их в систему в разделе &quot;<a
		href="/do/member/manual?page=content_management#custom_images"><u> Дополнительные изображения</u></a>&quot;.
</ul>
<br><br>Примечание 2: В разделе проекта приложения в wiki есть примеры динамических документов, блок &quot;конфигурация - дополнительные документы&quot; (&quot;configuration - custom documents&quot;). 
После создания документа установите 
<a href="/do/member/manual?page=groups#manage_group_permissions_member"><u>разрешения 
участников</u></a> из выпадающего списка в блоке разрешения к документам. 
<hr class="help">
</span>
 <span class="member">
<a name="member_document"></a>
<h3>Мои документы</h3>
Окно содержит список документов, которые администрация Системы разместила для вас. Эти документы можно скачать и распечатать.
<br><br>Обычно такой документ представляет собой бланк или анкету. При наличии необходимых настроек, документ может вначале содержать для вас поля к заполнению, после чего указанные вами данные будут отображены в самом документе. </span>
 <span class="broker admin">
Для администраций и брокеров также указан тип документа. Статические и динамические 
документы могут просматриваться только из этого окна (чтобы управлять ими перейдите в
&quot;Меню: Управление содержанием > документы&quot;); при этом
документы участником также управляются из этого окна. В таком случае, у вас есть 
следующие возможности:
<ul>
	<li><img border="0" src="/pages/images/view.gif" width="16" height="16">&nbsp;
	позволяет вам просмотреть документ
	<li><img border="0" src="/pages/images/edit.gif" width="16" height="16">&nbsp;
	позволяет вам изменить документ
	<li><img border="0" src="/pages/images/delete.gif" width="16" height="16">&nbsp;
	позволяет вам удалить документ.
</ul>
</span><hr class="help">

<span class="broker admin"> <a name="edit_member_document"></a>
<h3>Добавить / Изменить документ участника</h3>
На странице личных данных участника, в окне "Информация об участнике" можно перейти к документам участника.
В этом окне вы можете определить новый &quot;Статический&quot; документ для индивидуального участника.
Это может быть файл любого типа, например pdf или изображение. Если вы хотите его изменить, то вы можете просто перезаписать
предыдущий документ поверх, для чего нажмите кнопку &quot;изменить&quot;. Завершив изменения, нажмите кнопку
&quot;Выполнить&quot;, чтобы сохранить ваши изменения.
<ul>
	<li><b>Название:</b> просто предоставьте описательное название
	<li><b>Описание:</b>доступно только для администрации
	<li><b>Показывать:</b> здесь вы можете определить, для каких типов
	пользователей этот документ будет доступен к просмотру. Если вы выберите &quot;участникам&quot;, то участники также
	смогут увидеть документ. Если выбран брокерам, то только брокеры (и администрация с
	разрешениями) смогут увидеть документ. И, конечно, если выбраны 
	&quot;администраторы&quot;, то только администраторы смогут увидеть
	документ.
        <li><b>Используемый файл:</b> это текущий файл документа. Нажмите на ссылку,
	чтобы просмотреть его. Ссылки не будет, если вы используете это окно, чтобы создать новый документ участника.
	<li><b>Загрузить Файл:</b> Просто укажите здесь имя файла и полный маршрут. Для
	этого используйте кнопку &quot;Выбрать&quot;.
</ul>
</span>

<div class='help'>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
<br><br>
</div>
