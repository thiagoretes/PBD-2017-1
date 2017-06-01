var E_SERVER_ERROR = 'Error na communicação com o Servidor'
const {dialog} = require('electron').remote
var path = ""
document.getElementById('select-file').addEventListener('click', function () {
	dialog.showOpenDialog(function (fileNames) {
		if (fileNames === undefined) {
			console.log("No file selected");
		} else {
			app.filepath = fileNames[0];
			app.__vue__.tableName = fileNames[0].split('\\').pop().split('/').pop();
			console.log(fileNames);
			app.__vue__.loadDBF();

        }
    });

}, false);

document.getElementById('convert-sqlite').addEventListener('click', function () {
    dialog.showOpenDialog(function (fileNames) {
        if (fileNames === undefined) {
            console.log("No file selected");
        } else {
			var dbfpath = fileNames[0];
            dialog.showSaveDialog(function (fileNames) {
                if (fileNames === undefined) {
                    console.log("No file selected");
                } else {
					console.log(fileNames);
                    app.__vue__.convertDBF(dbfpath,fileNames);
                }
            });


        }
    });

}, false);

document.getElementById('select-sqlite').addEventListener('click', function () {
	dialog.showOpenDialog(function (fileNames) {
		if (fileNames === undefined) {
			console.log("No file selected");
		} else {
			app.filepath = fileNames[0];
			console.log(fileNames);
			app.__vue__.loadSQLite();

		}
	});
}, false);



// fields definition
var tableColumns = [
	{
		name: 'id',
		title: '',
		dataClass: 'text-center',
		callback: 'showDetailRow'
	},
	{
		name: 'name',
		sortField: 'name',
	},
	{
		name: 'email',
		sortField: 'email',
	},
	{
		name: 'nickname',
		sortField: 'nickname',
		callback: 'allCap'
	},
	{
		name: 'birthdate',
		sortField: 'birthdate',
		callback: 'formatDate|D/MM/Y'
	},
	{
		name: 'gender',
		sortField: 'gender',
		titleClass: 'text-center',
		dataClass: 'text-center',
		callback: 'gender'
	},
	{
		name: '__component:custom-action',
		title: "Component",
		titleClass: 'center aligned',
		dataClass: 'custom-action center aligned',
	},
	{
		name: '__actions',
		dataClass: 'text-center',
	}
]

Vue.config.debug = true

Vue.component('custom-action', {
	template: [
		'<div>',
		'<button @click="itemAction(\'view-item\', rowData)"><i class="glyphicon glyphicon-zoom-in"></i></button>',
		'<button @click="itemAction(\'edit-item\', rowData)"><i class="glyphicon glyphicon-pencil"></i></button>',
		'<button @click="itemAction(\'delete-item\', rowData)"><i class="glyphicon glyphicon-remove"></i></button>',
		'</div>'
	].join(''),
	props: {
		rowData: {
			type: Object,
			required: true
		}
	},
	methods: {
		itemAction: function (action, data) {
			sweetAlert('custom-action: ' + action, data.name)
		},
		onClick: function (event) {
			console.log('custom-action: on-click', event.target)
		},
		onDoubleClick: function (event) {
			console.log('custom-action: on-dblclick', event.target)
		}
	}
})

Vue.component('my-detail-row', {
	template: [
		'<div class="detail-row ui form" @click="onClick($event)">',
		'<div class="inline field">',
		'<label>Nome: </label>',
		'<span>{{rowData.name}}</span>',
		'</div>',
		'<div class="inline field">',
		'<label>Email: </label>',
		'<span>{{rowData.email}}</span>',
		'</div>',
		'<div class="inline field">',
		'<label>Apelido: </label>',
		'<span>{{rowData.nickname}}</span>',
		'</div>',
		'<div class="inline field">',
		'<label>Data de Nascimento: </label>',
		'<span>{{rowData.birthdate}}</span>',
		'</div>',
		'<div class="inline field">',
		'<label>Gênero: </label>',
		'<span>{{rowData.gender}}</span>',
		'</div>',
		'</div>',
	].join(''),
	props: {
		rowData: {
			type: Object,
			required: true
		}
	},
	methods: {
		onClick: function (event) {
			console.log('my-detail-row: on-click')
		}
	},
})

new Vue({
	el: '#app',
	data: {
		searchFor: '',
		fields: tableColumns,
		sortOrder: [{
			field: 'rowid',
			direction: 'asc'
		}],
		multiSort: true,
		isLoaded: 'block',
		tableName: 'Escolha um arquivo...',
		perPage: 10,
		paginationComponent: 'vuetable-pagination',
		paginationInfoTemplate: 'แสดง {from} ถึง {to} จากทั้งหมด {total} รายการ',
		itemActions: [
			{
				name: 'view-item',
				label: '',
				icon: 'glyphicon glyphicon-zoom-in',
				class: 'btn btn-info',
				extra: {'title': 'View', 'data-toggle': "tooltip", 'data-placement': "left"}
			},
			{
				name: 'edit-item',
				label: '',
				icon: 'glyphicon glyphicon-pencil',
				class: 'btn btn-warning',
				extra: {title: 'Edit', 'data-toggle': "tooltip", 'data-placement': "top"}
			},
			{
				name: 'delete-item',
				label: '',
				icon: 'glyphicon glyphicon-remove',
				class: 'btn btn-danger',
				extra: {title: 'Delete', 'data-toggle': "tooltip", 'data-placement': "right"}
			}
		],
		moreParams: [{
			path: '',
			order: '',
			col: ''
		}],
	},
	watch: {
		'perPage': function (val, oldVal) {
			this.$broadcast('vuetable:refresh')
		},
		'paginationComponent': function (val, oldVal) {
			this.$broadcast('vuetable:load-success', this.$refs.vuetable.tablePagination)
			this.paginationConfig(this.paginationComponent)
		}
	},
	methods: {
		/**
		 * Callback functions
		 */
		allCap: function (value) {
			return value.toUpperCase()
		},
		gender: function (value) {
			return value == 'M'
				? '<span class="label label-info"><i class="glyphicon glyphicon-star"></i> Male</span>'
				: '<span class="label label-success"><i class="glyphicon glyphicon-heart"></i> Female</span>'
		},
		formatDate: function (value, fmt) {
			if (value == null) return ''
			fmt = (typeof fmt == 'undefined') ? 'D MMM YYYY' : fmt
			return moment(value, 'YYYY-MM-DD').format(fmt)
		},
		showDetailRow: function (value) {
			var icon = this.$refs.vuetable.isVisibleDetailRow(value) ? 'glyphicon glyphicon-minus-sign' : 'glyphicon glyphicon-plus-sign'
			return [
				'<a class="show-detail-row">',
				'<i class="' + icon + '"></i>',
				'</a>'
			].join('')
		},
		/**
		 * Other functions
		 */
		loadDBF: function()
		{
			this.moreParams = [
				'path=' + app.filepath,
				'type=1'

			]
			this.$nextTick(function () {
				this.$broadcast('vuetable:refresh')
				tableColumns = this.fields;
			})

		},
		loadSQLite: function()
		{
			this.moreParams = [
				'path=' + app.filepath,
				'type=2'

			]
			this.$nextTick(function () {
				this.$broadcast('vuetable:refresh')
				tableColumns = this.fields;
			})

        },
        convertDBF: function(dbf_path, sqlitepath)
        {
        	var self = this;



                swal({
                    title: "Converter DBF?",
                    text: "Converter o arquivo " + dbf_path + " para sqlite? O arquivo será salvo como: " + sqlitepath + "\nA conversão pode levar algum tempo(Cerca de 1min/500MB de arquivo).",
                    type: "warning",
                    showCancelButton: true,
                    closeOnConfirm: true,
                    confirmButtonText: "Sim",
                    confirmButtonColor: "#1fb3ec"
                }, function() {
                    $.ajax(
                        {
                            type: "get",
                            url: "http://localhost:8080/dbfToSqlite",
                            data: "dbfpath="+dbf_path+"&sqlitepath="+sqlitepath,
                            beforeSend: function(){
                            	console.log(app.filepath);
                            	console.log(dbf_path);
                            	console.log(sqlitepath);
                                self.$broadcast('vuetable:show-loading')
                            },
                            complete: function(){
                                self.$broadcast('vuetable:hide-loading');
                            },
                            success: function(data){
                                swal("Sucesso!", data, "success");

                            },
                            error: function(data)
							{
                                swal("Oops", "Algum erro ocorreu!\n"+data, "error");
							}
                        }
                    )
                        /*.done(function(data) {
                            swal("Sucesso!", data, "success");

                        })*/
                        /*.error(function(data) {

                        });*/
                });

            /*this.moreParams = [
                'path=' + dbf_path,
				'path2=' + app.filepath,
                'type=2'

            ]
            /*this.$nextTick(function () {
                this.$broadcast('vuetable:refresh')
                tableColumns = this.fields;
            })*/

        },
		setFilter: function () {
			this.moreParams = [
				'filter=' + this.searchFor
			]
			this.$nextTick(function () {
				this.$broadcast('vuetable:refresh')
			})
		},
		resetFilter: function () {
			this.searchFor = ''
			this.setFilter()
		},
		preg_quote: function (str) {
			// http://kevin.vanzonneveld.net
			// +   original by: booeyOH
			// +   improved by: Ates Goral (http://magnetiq.com)
			// +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
			// +   bugfixed by: Onno Marsman
			// *     example 1: preg_quote("$40");
			// *     returns 1: '\$40'
			// *     example 2: preg_quote("*RRRING* Hello?");
			// *     returns 2: '\*RRRING\* Hello\?'
			// *     example 3: preg_quote("\\.+*?[^]$(){}=!<>|:");
			// *     returns 3: '\\\.\+\*\?\[\^\]\$\(\)\{\}\=\!\<\>\|\:'

			return (str + '').replace(/([\\\.\+\*\?\[\^\]\$\(\)\{\}\=\!\<\>\|\:])/g, "\\$1");
		},
		highlight: function (needle, haystack) {
			return haystack.replace(
				new RegExp('(' + this.preg_quote(needle) + ')', 'ig'),
				'<span class="highlight">$1</span>'
			)
		},
		makeDetailRow: function (data) {
			return [
				'<td colspan="7">',
				'<div class="detail-row">',
				'<div class="form-group">',
				'<label>Nome: </label>',
				'<span>' + data.name + '</span>',
				'</div>',
				'<div class="form-group">',
				'<label>Email: </label>',
				'<span>' + data.email + '</span>',
				'</div>',
				'<div class="form-group">',
				'<label>Apelido: </label>',
				'<span>' + data.nickname + '</span>',
				'</div>',
				'<div class="form-group">',
				'<label>Data de Nascimento: </label>',
				'<span>' + data.birthdate + '</span>',
				'</div>',
				'<div class="form-group">',
				'<label>Gênero: </label>',
				'<span>' + data.gender + '</span>',
				'</div>',
				'</div>',
				'</td>'
			].join('')
		},
		rowClassCB: function (data, index) {
			return (index % 2) === 0 ? 'positive' : ''
		},
		paginationConfig: function (componentName) {
			console.log('paginationConfig: ', componentName)
			if (componentName == 'vuetable-pagination') {
				this.$broadcast('vuetable-pagination:set-options', {
					wrapperClass: 'pagination',
					icons: {first: '', prev: '', next: '', last: ''},
					activeClass: 'active',
					linkClass: 'btn btn-default',
					pageClass: 'btn btn-default'
				})
			}
			if (componentName == 'vuetable-pagination-dropdown') {
				this.$broadcast('vuetable-pagination:set-options', {
					wrapperClass: 'form-inline',
					icons: {prev: 'glyphicon glyphicon-chevron-left', next: 'glyphicon glyphicon-chevron-right'},
					dropdownClass: 'form-control'
				})
			}
		},
		// -------------------------------------------------------------------------------------------
		// You can change how sort params string is constructed by overriding getSortParam() like this
		// -------------------------------------------------------------------------------------------
		// getSortParam: function(sortOrder) {
		//     console.log('parent getSortParam:', JSON.stringify(sortOrder))
		//     return sortOrder.map(function(sort) {
		//         return (sort.direction === 'desc' ? '+' : '') + sort.field
		//     }).join(',')
		// }
	},
	events: {
		'vuetable:row-changed': function (data) {
			console.log('row-changed:', data.name)
		},
		'vuetable:row-clicked': function (data, event) {
			console.log('row-clicked:', data.name)
		},
		'vuetable:cell-clicked': function (data, field, event) {
			console.log('cell-clicked:', field.name)
			if (field.name !== '__actions') {
				this.$broadcast('vuetable:toggle-detail', data.id)
			}
		},
		'vuetable:action': function (action, data) {
			console.log('vuetable:action', action, data)
			if (action == 'view-item') {
				sweetAlert(action, data.name)
			} else if (action == 'edit-item') {
				sweetAlert(action, data.name)
			} else if (action == 'delete-item') {
				sweetAlert(action, data.name)
			}
		},
		'vuetable:load-success': function (response) {
			var data = response.data.data
			console.log(data)
			if (this.searchFor !== '') {
				for (n in data) {
					data[n].name = this.highlight(this.searchFor, data[n].name)
					data[n].email = this.highlight(this.searchFor, data[n].email)
				}
			}
		},
		'vuetable:load-error': function (response) {
			if (response.status == 400) {
				sweetAlert('Something\'s Wrong!', response.data.message, 'error')
			} else {
				sweetAlert('Opa', E_SERVER_ERROR)
			}
		}
	}
})
