const { useState, useEffect } = React;
const { Layout, Menu, Table, Button, Form, Input, InputNumber, Select, DatePicker,
        Modal, message, Tag, Card, Row, Col, Statistic, Tabs, List, Avatar,
        Upload, Descriptions, Popconfirm, Space, Tooltip } = antd;
const { Header, Content, Sider } = Layout;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;
const { Item } = Form.List;

const API_BASE = '';

const getToken = () => localStorage.getItem('invoice_archive_token');
const setToken = (token) => localStorage.setItem('invoice_archive_token', token);
const clearToken = () => localStorage.removeItem('invoice_archive_token');

const StatusTag = ({ status }) => {
    const statusMap = {
        UPLOADED: { text: '已上传', className: 'status-uploaded' },
        ASSOCIATED: { text: '已关联', className: 'status-associated' },
        ARCHIVED: { text: '已归档', className: 'status-archived' },
        RETURNED: { text: '已退回', className: 'status-returned' },
        SEALED: { text: '已封存', className: 'status-sealed' }
    };
    const info = statusMap[status] || { text: status, className: 'status-uploaded' };
    return <span className={`status-tag ${info.className}`}>{info.text}</span>;
};

const RoleTag = ({ role }) => {
    const roleMap = {
        ACCOUNTANT: { text: '会计', color: 'blue' },
        DEPT_HANDLER: { text: '部门经办人', color: 'green' },
        ARCHIVIST: { text: '档案员', color: 'orange' },
        AUDITOR: { text: '审计查询人', color: 'purple' }
    };
    const info = roleMap[role] || { text: role, color: 'default' };
    return <Tag color={info.color}>{info.text}</Tag>;
};

const api = {
    request: async (url, options = {}) => {
        const headers = { 'Content-Type': 'application/json' };
        const token = getToken();
        if (token) {
            headers['Authorization'] = token;
        }
        const defaultOptions = {
            headers,
            ...options
        };
        const response = await fetch(`${API_BASE}${url}`, defaultOptions);
        if (response.status === 401) {
            clearToken();
            message.error('登录已过期，请重新登录');
            window.location.reload();
            throw new Error('Unauthorized');
        }
        const data = await response.json();
        if (data.code !== 200) {
            message.error(data.message || '请求失败');
            throw new Error(data.message);
        }
        return data.data;
    },
    get: (url) => api.request(url, { method: 'GET' }),
    post: (url, data) => api.request(url, {
        method: 'POST',
        body: JSON.stringify(data)
    })
};

const LoginPage = ({ onLogin }) => {
    const [loading, setLoading] = useState(false);

    const handleLogin = async (values) => {
        setLoading(true);
        try {
            const result = await api.post('/api/user/login', values);
            setToken(result.token);
            message.success('登录成功');
            onLogin(result.user);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-title">
                <h2>财税票据归档系统</h2>
                <p>财务共享中心归档责任链</p>
            </div>
            <Form onFinish={handleLogin} layout="vertical">
                <Form.Item
                    label="用户名"
                    name="username"
                    initialValue="accountant"
                    rules={[{ required: true, message: '请输入用户名' }]}
                >
                    <Input placeholder="请输入用户名" />
                </Form.Item>
                <Form.Item
                    label="密码"
                    name="password"
                    initialValue="123456"
                    rules={[{ required: true, message: '请输入密码' }]}
                >
                    <Input.Password placeholder="请输入密码" />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading} block>
                        登 录
                    </Button>
                </Form.Item>
            </Form>
            <div style={{ marginTop: 16, fontSize: 12, color: '#8c8c8c' }}>
                <p>测试账号：</p>
                <p>会计：accountant / 123456</p>
                <p>经办人：handler / 123456</p>
                <p>档案员：archivist / 123456</p>
                <p>审计：auditor / 123456</p>
            </div>
        </div>
    );
};

const UploadPage = ({ user }) => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);

    const handleUpload = async (values) => {
        setLoading(true);
        try {
            values.imageUrl = '/images/invoice-sample.jpg';
            values.imageMd5 = 'd41d8cd98f00b204e9800998ecf8427e';
            values.imageSize = 102400;
            await api.post('/api/invoice/upload', values);
            message.success('票据上传成功');
            form.resetFields();
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-card">
            <h2 className="page-title">票据上传</h2>
            <Form form={form} onFinish={handleUpload} layout="vertical" style={{ maxWidth: 600 }}>
                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item
                            label="票据代码"
                            name="invoiceCode"
                            rules={[{ required: true, message: '请输入票据代码' }]}
                        >
                            <Input placeholder="请输入票据代码" />
                        </Form.Item>
                    </Col>
                    <Col span={12}>
                        <Form.Item
                            label="票据号码"
                            name="invoiceNumber"
                        >
                            <Input placeholder="请输入票据号码" />
                        </Form.Item>
                    </Col>
                </Row>
                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item
                            label="开票日期"
                            name="invoiceDate"
                        >
                            <DatePicker style={{ width: '100%' }} />
                        </Form.Item>
                    </Col>
                    <Col span={12}>
                        <Form.Item
                            label="金额"
                            name="amount"
                            rules={[{ required: true, message: '请输入金额' }]}
                        >
                            <InputNumber style={{ width: '100%' }} placeholder="请输入金额" min={0} precision={2} />
                        </Form.Item>
                    </Col>
                </Row>
                <Row gutter={16}>
                    <Col span={12}>
                        <Form.Item label="税额" name="taxAmount">
                            <InputNumber style={{ width: '100%' }} placeholder="请输入税额" min={0} precision={2} />
                        </Form.Item>
                    </Col>
                    <Col span={12}>
                        <Form.Item label="价税合计" name="totalAmount">
                            <InputNumber style={{ width: '100%' }} placeholder="请输入价税合计" min={0} precision={2} />
                        </Form.Item>
                    </Col>
                </Row>
                <Form.Item
                    label="开票方"
                    name="sellerName"
                    rules={[{ required: true, message: '请输入开票方' }]}
                >
                    <Input placeholder="请输入开票方名称" />
                </Form.Item>
                <Form.Item label="开票方税号" name="sellerTaxNumber">
                    <Input placeholder="请输入开票方税号" />
                </Form.Item>
                <Form.Item label="购买方" name="buyerName">
                    <Input placeholder="请输入购买方名称" />
                </Form.Item>
                <Form.Item label="购买方税号" name="buyerTaxNumber">
                    <Input placeholder="请输入购买方税号" />
                </Form.Item>
                <Form.Item
                    label="票据影像"
                    name="imageUrl"
                    rules={[{ required: true, message: '请上传票据影像' }]}
                >
                    <div className="image-placeholder">
                        点击上传票据影像（模拟）
                    </div>
                </Form.Item>
                <Form.Item label="备注" name="remark">
                    <TextArea rows={3} placeholder="请输入备注" />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading}>
                        提交上传
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
};

const InvoiceListPage = ({ user, onViewDetail }) => {
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(false);
    const [statusFilter, setStatusFilter] = useState();
    const [sellerName, setSellerName] = useState('');

    const loadInvoices = async () => {
        setLoading(true);
        try {
            let url = '/api/invoice/list';
            const params = new URLSearchParams();
            if (statusFilter) params.append('status', statusFilter);
            if (sellerName) params.append('sellerName', sellerName);
            if (params.toString()) url += '?' + params.toString();
            const data = await api.get(url);
            setInvoices(data);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadInvoices();
    }, [statusFilter]);

    const columns = [
        { title: '票据代码', dataIndex: 'invoiceCode', key: 'invoiceCode' },
        { title: '开票方', dataIndex: 'sellerName', key: 'sellerName' },
        { title: '金额', dataIndex: 'amount', key: 'amount', render: v => `¥${v}` },
        { title: '状态', dataIndex: 'status', key: 'status', render: s => <StatusTag status={s} /> },
        { title: '上传部门', dataIndex: 'uploadDept', key: 'uploadDept' },
        { title: '上传人', dataIndex: 'uploaderName', key: 'uploaderName' },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => (
                <Button type="link" onClick={() => onViewDetail(record.id)}>
                    查看详情
                </Button>
            )
        }
    ];

    return (
        <div className="page-card">
            <h2 className="page-title">票据检索</h2>
            <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}>
                    <Select
                        placeholder="选择状态"
                        allowClear
                        style={{ width: '100%' }}
                        onChange={setStatusFilter}
                    >
                        <Option value="UPLOADED">已上传</Option>
                        <Option value="ASSOCIATED">已关联</Option>
                        <Option value="ARCHIVED">已归档</Option>
                        <Option value="RETURNED">已退回</Option>
                        <Option value="SEALED">已封存</Option>
                    </Select>
                </Col>
                <Col span={6}>
                    <Input.Search
                        placeholder="搜索开票方"
                        onSearch={value => { setSellerName(value); loadInvoices(); }}
                        enterButton
                    />
                </Col>
                <Col span={6}>
                    <Button onClick={loadInvoices}>刷新</Button>
                </Col>
            </Row>
            <Table
                columns={columns}
                dataSource={invoices}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10 }}
            />
        </div>
    );
};

const InvoiceDetailModal = ({ invoiceId, visible, onClose }) => {
    const [invoice, setInvoice] = useState(null);
    const [versions, setVersions] = useState([]);
    const [auditLogs, setAuditLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('info');

    useEffect(() => {
        if (visible && invoiceId) {
            loadDetail();
        }
    }, [visible, invoiceId]);

    const loadDetail = async () => {
        setLoading(true);
        try {
            const inv = await api.get(`/api/invoice/${invoiceId}`);
            setInvoice(inv);
            const vers = await api.get(`/api/invoice/${invoiceId}/versions`);
            setVersions(vers);
            const logs = await api.get(`/api/audit/timeline/invoice/${invoiceId}`);
            setAuditLogs(logs);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    if (!visible) return null;

    return (
        <Modal
            title="票据详情"
            visible={visible}
            onCancel={onClose}
            width={800}
            footer={[<Button key="close" onClick={onClose}>关闭</Button>]}
        >
            {invoice && (
                <Tabs activeKey={activeTab} onChange={setActiveTab}>
                    <TabPane tab="基本信息" key="info">
                        <Descriptions column={2} bordered size="small">
                            <Descriptions.Item label="票据代码">{invoice.invoiceCode}</Descriptions.Item>
                            <Descriptions.Item label="票据号码">{invoice.invoiceNumber || '-'}</Descriptions.Item>
                            <Descriptions.Item label="开票方">{invoice.sellerName}</Descriptions.Item>
                            <Descriptions.Item label="金额">¥{invoice.amount}</Descriptions.Item>
                            <Descriptions.Item label="状态"><StatusTag status={invoice.status} /></Descriptions.Item>
                            <Descriptions.Item label="影像版本">v{invoice.imageVersion}</Descriptions.Item>
                            <Descriptions.Item label="上传部门">{invoice.uploadDept}</Descriptions.Item>
                            <Descriptions.Item label="上传人">{invoice.uploaderName}</Descriptions.Item>
                            <Descriptions.Item label="关联报销单">{invoice.reimburseBillNo || '未关联'}</Descriptions.Item>
                            <Descriptions.Item label="归档盒">{invoice.archiveBoxNo || '未归档'}</Descriptions.Item>
                        </Descriptions>
                        <div style={{ marginTop: 16 }}>
                            <div className="image-placeholder">
                                票据影像预览（模拟）
                            </div>
                        </div>
                    </TabPane>
                    <TabPane tab="影像版本" key="versions">
                        <List
                            dataSource={versions}
                            renderItem={item => (
                                <List.Item key={item.id}>
                                    <List.Item.Meta
                                        title={`版本 v${item.versionNumber}`}
                                        description={`上传人：${item.uploaderName} | 上传时间：${item.createTime} | ${item.changeReason || ''}`}
                                    />
                                </List.Item>
                            )}
                        />
                    </TabPane>
                    <TabPane tab="审计时间线" key="timeline">
                        {auditLogs.map((log, index) => (
                            <div key={log.id} className="timeline-item">
                                <div className="timeline-time">{log.createTime}</div>
                                <div className="timeline-content">{log.operationDesc}</div>
                                <div className="timeline-operator">
                                    操作人：{log.operatorName}
                                    {log.operatorRole && <RoleTag role={log.operatorRole} />}
                                </div>
                            </div>
                        ))}
                        {auditLogs.length === 0 && <p style={{ color: '#8c8c8c' }}>暂无审计记录</p>}
                    </TabPane>
                </Tabs>
            )}
        </Modal>
    );
};

const ReimbursePage = ({ user }) => {
    const [bills, setBills] = useState([]);
    const [invoices, setInvoices] = useState([]);
    const [selectedBill, setSelectedBill] = useState(null);
    const [associateModalVisible, setAssociateModalVisible] = useState(false);
    const [selectedInvoiceId, setSelectedInvoiceId] = useState(null);
    const [form] = Form.useForm();

    const loadBills = async () => {
        try {
            const data = await api.get('/api/reimburse/bills');
            setBills(data);
        } catch (e) {
            console.error(e);
        }
    };

    const loadUploadedInvoices = async () => {
        try {
            const data = await api.get('/api/invoice/list?status=UPLOADED');
            setInvoices(data);
        } catch (e) {
            console.error(e);
        }
    };

    useEffect(() => {
        loadBills();
        loadUploadedInvoices();
    }, []);

    const handleAssociate = async () => {
        try {
            await api.post('/api/reimburse/associate', {
                invoiceId: selectedInvoiceId,
                reimburseBillId: selectedBill.id
            });
            message.success('关联成功');
            setAssociateModalVisible(false);
            loadBills();
            loadUploadedInvoices();
        } catch (e) {
            console.error(e);
        }
    };

    const billColumns = [
        { title: '报销单号', dataIndex: 'billNo', key: 'billNo' },
        { title: '类型', dataIndex: 'billType', key: 'billType' },
        { title: '金额', dataIndex: 'amount', key: 'amount', render: v => `¥${v}` },
        { title: '状态', dataIndex: 'billStatus', key: 'billStatus' },
        { title: '部门', dataIndex: 'department', key: 'department' },
        { title: '申请人', dataIndex: 'applicantName', key: 'applicantName' }
    ];

    const invoiceColumns = [
        { title: '票据代码', dataIndex: 'invoiceCode', key: 'invoiceCode' },
        { title: '开票方', dataIndex: 'sellerName', key: 'sellerName' },
        { title: '金额', dataIndex: 'amount', key: 'amount', render: v => `¥${v}` },
        { title: '状态', dataIndex: 'status', key: 'status', render: s => <StatusTag status={s} /> }
    ];

    return (
        <div className="page-card">
            <h2 className="page-title">报销单关联</h2>
            <Tabs defaultActiveKey="bills">
                <TabPane tab="报销单列表" key="bills">
                    <Table
                        columns={billColumns}
                        dataSource={bills}
                        rowKey="id"
                        onRow={record => ({
                            onClick: () => setSelectedBill(record)
                        })}
                        pagination={{ pageSize: 10 }}
                    />
                </TabPane>
                <TabPane tab="待关联票据" key="invoices">
                    <Row justify="end" style={{ marginBottom: 16 }}>
                        <Button
                            type="primary"
                            onClick={() => setAssociateModalVisible(true)}
                            disabled={invoices.length === 0}
                        >
                            关联报销单
                        </Button>
                    </Row>
                    <Table
                        columns={invoiceColumns}
                        dataSource={invoices}
                        rowKey="id"
                        rowSelection={{
                            type: 'radio',
                            onChange: (_, selectedRows) => {
                                if (selectedRows.length > 0) {
                                    setSelectedInvoiceId(selectedRows[0].id);
                                }
                            }
                        }}
                        pagination={{ pageSize: 10 }}
                    />
                </TabPane>
            </Tabs>

            <Modal
                title="选择报销单进行关联"
                visible={associateModalVisible}
                onCancel={() => setAssociateModalVisible(false)}
                onOk={handleAssociate}
                width={700}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        label="选择报销单"
                        name="reimburseBillId"
                        rules={[{ required: true, message: '请选择报销单' }]}
                    >
                        <Select placeholder="请选择报销单" style={{ width: '100%' }}>
                            {bills.map(bill => (
                                <Option key={bill.id} value={bill.id}>
                                    {bill.billNo} - {bill.billType} - ¥{bill.amount}
                                </Option>
                            ))}
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

const ArchivePage = ({ user }) => {
    const [records, setRecords] = useState([]);
    const [batches, setBatches] = useState([]);
    const [archiveModalVisible, setArchiveModalVisible] = useState(false);
    const [returnModalVisible, setReturnModalVisible] = useState(false);
    const [selectedInvoiceId, setSelectedInvoiceId] = useState(null);
    const [invoices, setInvoices] = useState([]);
    const [form] = Form.useForm();
    const [returnForm] = Form.useForm();

    const loadData = async () => {
        try {
            const recs = await api.get('/api/archive/records');
            setRecords(recs);
            const bts = await api.get('/api/archive/batches');
            setBatches(bts);
            const invs = await api.get('/api/invoice/list?status=ASSOCIATED');
            setInvoices(invs);
        } catch (e) {
            console.error(e);
        }
    };

    useEffect(() => {
        loadData();
    }, []);

    const handleArchive = async (values) => {
        try {
            await api.post('/api/archive/archive', {
                invoiceId: selectedInvoiceId,
                archiveBoxNo: values.archiveBoxNo,
                archivePosition: values.archivePosition,
                archiveBatchId: values.archiveBatchId
            });
            message.success('归档成功');
            setArchiveModalVisible(false);
            form.resetFields();
            loadData();
        } catch (e) {
            console.error(e);
        }
    };

    const handleReturn = async (values) => {
        try {
            await api.post('/api/archive/return', {
                invoiceId: selectedInvoiceId,
                returnReason: values.returnReason
            });
            message.success('退回成功');
            setReturnModalVisible(false);
            returnForm.resetFields();
            loadData();
        } catch (e) {
            console.error(e);
        }
    };

    const recordColumns = [
        { title: '归档编号', dataIndex: 'archiveNo', key: 'archiveNo' },
        { title: '票据代码', dataIndex: 'invoiceCode', key: 'invoiceCode' },
        { title: '归档盒', dataIndex: 'archiveBoxNo', key: 'archiveBoxNo' },
        { title: '位置', dataIndex: 'archivePosition', key: 'archivePosition' },
        { title: '档案员', dataIndex: 'archivistName', key: 'archivistName' },
        { title: '归档时间', dataIndex: 'archiveTime', key: 'archiveTime' }
    ];

    const batchColumns = [
        { title: '批次号', dataIndex: 'batchNo', key: 'batchNo' },
        { title: '批次名称', dataIndex: 'batchName', key: 'batchName' },
        { title: '票据数量', dataIndex: 'invoiceCount', key: 'invoiceCount' },
        { title: '状态', dataIndex: 'sealed', key: 'sealed', render: s => s ? <Tag color="default">已封存</Tag> : <Tag color="green">正常</Tag> },
        { title: '创建人', dataIndex: 'archivistName', key: 'archivistName' },
        {
            title: '操作',
            key: 'action',
            render: (_, record) => !record.sealed && (
                <Popconfirm
                    title="确认封存该批次？"
                    description="封存后将无法修改或新增票据"
                    onConfirm={async () => {
                        try {
                            await api.post('/api/archive/batch/seal', { batchId: record.id });
                            message.success('封存成功');
                            loadData();
                        } catch (e) { console.error(e); }
                    }}
                >
                    <Button type="link">封存</Button>
                </Popconfirm>
            )
        }
    ];

    return (
        <div className="page-card">
            <h2 className="page-title">归档管理</h2>
            <Tabs defaultActiveKey="records">
                <TabPane tab="归档记录" key="records">
                    <Row justify="end" style={{ marginBottom: 16 }}>
                        <Button type="primary" onClick={() => setArchiveModalVisible(true)}>
                            新增归档
                        </Button>
                    </Row>
                    <Table
                        columns={recordColumns}
                        dataSource={records}
                        rowKey="id"
                        pagination={{ pageSize: 10 }}
                    />
                </TabPane>
                <TabPane tab="归档批次" key="batches">
                    <Row justify="end" style={{ marginBottom: 16 }}>
                        <Button type="primary" onClick={async () => {
                            try {
                                await api.post('/api/archive/batch', { batchName: '新批次' });
                                message.success('批次创建成功');
                                loadData();
                            } catch (e) { console.error(e); }
                        }}>
                            创建批次
                        </Button>
                    </Row>
                    <Table
                        columns={batchColumns}
                        dataSource={batches}
                        rowKey="id"
                        pagination={{ pageSize: 10 }}
                    />
                </TabPane>
                <TabPane tab="退回补扫" key="returns">
                    <p>可从归档记录中选择已归档的票据进行退回操作</p>
                </TabPane>
            </Tabs>

            <Modal
                title="票据归档"
                visible={archiveModalVisible}
                onCancel={() => setArchiveModalVisible(false)}
                onOk={() => form.submit()}
                width={600}
            >
                <Form form={form} onFinish={handleArchive} layout="vertical">
                    <Form.Item
                        label="选择票据"
                        name="invoiceId"
                        rules={[{ required: true, message: '请选择票据' }]}
                    >
                        <Select
                            placeholder="请选择待归档票据（已关联状态）"
                            style={{ width: '100%' }}
                            onChange={(val) => setSelectedInvoiceId(val)}
                        >
                            {invoices.map(inv => (
                                <Option key={inv.id} value={inv.id}>
                                    {inv.invoiceCode} - {inv.sellerName} - ¥{inv.amount}
                                </Option>
                            ))}
                        </Select>
                    </Form.Item>
                    <Form.Item
                        label="归档盒编号"
                        name="archiveBoxNo"
                        rules={[{ required: true, message: '请输入归档盒编号' }]}
                    >
                        <Input placeholder="例如：BOX-001" />
                    </Form.Item>
                    <Form.Item
                        label="存放位置"
                        name="archivePosition"
                        rules={[{ required: true, message: '请输入存放位置' }]}
                    >
                        <Input placeholder="例如：A区-3层-2列" />
                    </Form.Item>
                    <Form.Item label="归档批次" name="archiveBatchId">
                        <Select placeholder="请选择归档批次（可选）" allowClear style={{ width: '100%' }}>
                            {batches.filter(b => !b.sealed).map(batch => (
                                <Option key={batch.id} value={batch.id}>
                                    {batch.batchNo} - {batch.batchName}
                                </Option>
                            ))}
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="退回补扫"
                visible={returnModalVisible}
                onCancel={() => setReturnModalVisible(false)}
                onOk={() => returnForm.submit()}
                width={500}
            >
                <Form form={returnForm} onFinish={handleReturn} layout="vertical">
                    <Form.Item
                        label="退回原因"
                        name="returnReason"
                        rules={[{ required: true, message: '请输入退回原因' }]}
                    >
                        <TextArea rows={4} placeholder="请输入退回原因" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

const AuditPage = ({ user }) => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);

    const loadLogs = async () => {
        setLoading(true);
        try {
            const data = await api.get('/api/audit/logs');
            setLogs(data);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadLogs();
    }, []);

    const columns = [
        { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
        { title: '票据代码', dataIndex: 'invoiceCode', key: 'invoiceCode' },
        { title: '操作类型', dataIndex: 'operationType', key: 'operationType' },
        { title: '操作描述', dataIndex: 'operationDesc', key: 'operationDesc' },
        { title: '操作人', dataIndex: 'operatorName', key: 'operatorName' },
        { title: '角色', dataIndex: 'operatorRole', key: 'operatorRole', render: r => r ? <RoleTag role={r} /> : '-' },
        { title: '前状态', dataIndex: 'beforeStatus', key: 'beforeStatus', render: s => s ? <StatusTag status={s} /> : '-' },
        { title: '后状态', dataIndex: 'afterStatus', key: 'afterStatus', render: s => s ? <StatusTag status={s} /> : '-' }
    ];

    return (
        <div className="page-card">
            <h2 className="page-title">审计查询</h2>
            <Table
                columns={columns}
                dataSource={logs}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 20 }}
            />
        </div>
    );
};

const DashboardPage = ({ user }) => {
    const [stats, setStats] = useState({
        total: 0,
        uploaded: 0,
        associated: 0,
        archived: 0
    });
    const [recentLogs, setRecentLogs] = useState([]);

    const loadStats = async () => {
        try {
            const all = await api.get('/api/invoice/list');
            const uploaded = all.filter(i => i.status === 'UPLOADED' || i.status === 'RETURNED').length;
            const associated = all.filter(i => i.status === 'ASSOCIATED').length;
            const archived = all.filter(i => i.status === 'ARCHIVED' || i.status === 'SEALED').length;
            setStats({
                total: all.length,
                uploaded,
                associated,
                archived
            });
        } catch (e) {
            console.error(e);
        }
    };

    const loadRecentLogs = async () => {
        try {
            const logs = await api.get('/api/audit/logs');
            setRecentLogs(logs.slice(0, 10));
        } catch (e) {
            console.error(e);
        }
    };

    useEffect(() => {
        loadStats();
        loadRecentLogs();
    }, []);

    return (
        <div>
            <Row gutter={16} style={{ marginBottom: 20 }}>
                <Col span={6}>
                    <div className="stat-card">
                        <div className="stat-number" style={{ color: '#1890ff' }}>{stats.total}</div>
                        <div className="stat-label">票据总数</div>
                    </div>
                </Col>
                <Col span={6}>
                    <div className="stat-card">
                        <div className="stat-number" style={{ color: '#52c41a' }}>{stats.uploaded}</div>
                        <div className="stat-label">待关联</div>
                    </div>
                </Col>
                <Col span={6}>
                    <div className="stat-card">
                        <div className="stat-number" style={{ color: '#fa8c16' }}>{stats.associated}</div>
                        <div className="stat-label">待归档</div>
                    </div>
                </Col>
                <Col span={6}>
                    <div className="stat-card">
                        <div className="stat-number" style={{ color: '#722ed1' }}>{stats.archived}</div>
                        <div className="stat-label">已归档</div>
                    </div>
                </Col>
            </Row>
            <div className="page-card">
                <h2 className="page-title">最近操作记录</h2>
                <List
                    dataSource={recentLogs}
                    renderItem={item => (
                        <List.Item key={item.id}>
                            <List.Item.Meta
                                title={item.operationDesc}
                                description={`${item.createTime} - ${item.operatorName}`}
                            />
                            {item.afterStatus && <StatusTag status={item.afterStatus} />}
                        </List.Item>
                    )}
                />
            </div>
        </div>
    );
};

const App = () => {
    const [user, setUser] = useState(null);
    const [currentPage, setCurrentPage] = useState('dashboard');
    const [detailModalVisible, setDetailModalVisible] = useState(false);
    const [selectedInvoiceId, setSelectedInvoiceId] = useState(null);

    const handleLogin = (userData) => {
        setUser(userData);
    };

    const handleLogout = () => {
        clearToken();
        setUser(null);
        setCurrentPage('dashboard');
    };

    const handleViewDetail = (invoiceId) => {
        setSelectedInvoiceId(invoiceId);
        setDetailModalVisible(true);
    };

    const getMenuItems = () => {
        const items = [
            { key: 'dashboard', icon: '📊', label: '工作台' }
        ];

        if (user.roles.includes('ACCOUNTANT')) {
            items.push({ key: 'upload', icon: '📤', label: '票据上传' });
        }

        if (user.roles.includes('DEPT_HANDLER')) {
            items.push({ key: 'reimburse', icon: '📋', label: '报销单关联' });
        }

        if (user.roles.includes('ARCHIVIST')) {
            items.push({ key: 'archive', icon: '📦', label: '归档管理' });
        }

        items.push({ key: 'invoices', icon: '🔍', label: '票据检索' });

        if (user.roles.includes('AUDITOR') || user.roles.includes('ARCHIVIST')) {
            items.push({ key: 'audit', icon: '📝', label: '审计查询' });
        }

        return items;
    };

    if (!user) {
        return <LoginPage onLogin={handleLogin} />;
    }

    const renderPage = () => {
        switch (currentPage) {
            case 'dashboard': return <DashboardPage user={user} />;
            case 'upload': return <UploadPage user={user} />;
            case 'reimburse': return <ReimbursePage user={user} />;
            case 'archive': return <ArchivePage user={user} />;
            case 'invoices': return <InvoiceListPage user={user} onViewDetail={handleViewDetail} />;
            case 'audit': return <AuditPage user={user} />;
            default: return <DashboardPage user={user} />;
        }
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Header className="app-header">
                <h1>📄 财税票据归档系统</h1>
                <div className="user-info">
                    <span>{user.realName}</span>
                    {user.roles.map((role, idx) => (
                        <RoleTag key={idx} role={role} />
                    ))}
                    <Button size="small" onClick={handleLogout}>退出</Button>
                </div>
            </Header>
            <Layout>
                <Sider width={200} style={{ background: '#fff' }}>
                    <Menu
                        mode="inline"
                        selectedKeys={[currentPage]}
                        onClick={({ key }) => setCurrentPage(key)}
                        style={{ height: '100%', borderRight: 0 }}
                    >
                        {getMenuItems().map(item => (
                            <Menu.Item key={item.key}>
                                {item.icon} {item.label}
                            </Menu.Item>
                        ))}
                    </Menu>
                </Sider>
                <Content className="main-container">
                    {renderPage()}
                </Content>
            </Layout>

            <InvoiceDetailModal
                invoiceId={selectedInvoiceId}
                visible={detailModalVisible}
                onClose={() => setDetailModalVisible(false)}
            />
        </Layout>
    );
};

ReactDOM.render(<App />, document.getElementById('root'));
