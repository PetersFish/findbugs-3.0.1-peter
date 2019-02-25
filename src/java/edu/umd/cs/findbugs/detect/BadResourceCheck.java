package edu.umd.cs.findbugs.detect;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.ba.*;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.*;
import edu.umd.cs.findbugs.detect.database.*;
import edu.umd.cs.findbugs.detect.database.container.BitSetBuffer;
import edu.umd.cs.findbugs.detect.database.container.LinkedStack;
import edu.umd.cs.findbugs.util.OpcodeUtils;
import edu.umd.cs.findbugs.util.SignatureUtils;
import org.apache.bcel.classfile.*;
import edu.umd.cs.findbugs.classfile.Global;

import java.util.*;

/**
 * @author Peter Yu
 * @date 2018/6/1 17:32
 */
public class BadResourceCheck extends OpcodeStackDetector {

    public BadResourceCheck(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    public BadResourceCheck(BugReporter bugReporter,
                            Integer currentScanLevel,
                            Method currentSpecifiedMethod,
                            Integer currentLookIntoLevel,
                            HashMap<Integer,ResourceTarget> upperLevelResourceRegMap) {
        this.bugReporter = bugReporter;
        this.currentScanLevel = currentScanLevel;
        this.currentSpecifiedMethod = currentSpecifiedMethod;
        this.currentLookIntoLevel = currentLookIntoLevel;
        this.upperLevelResourceRegMap = upperLevelResourceRegMap;
    }

    private IAnalysisCache analysisCache = Global.getAnalysisCache();

    private BugReporter bugReporter;

    private static final boolean DEBUG = SystemProperties.getBoolean("rnr.debug");

    // ++++++++++++++++ IfElseBlock 相关 +++++++++++++++++++++++

    private IfElseBranchManager branchManager;

    private static final LinkedStack<IfElseBranchManager> branchManagerStack = new LinkedStack<>();

    private IfElseBlockManager blockManager;

    private static final LinkedStack<IfElseBlockManager> blockManagerStack = new LinkedStack<>();

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // ++++++++++++++++ close方法检测 相关 +++++++++++++++++++++++

    // 以下两个参数只在isResourceCloseInvoke方法范围内有效，方法结束后要重置
    /**
     * 用于存放当前层特定方法的参数及其index
     */
    private HashMap<Integer,ResourceTarget> currentLikeResourceRegMap;

    /**
     * 用于接受上一层传递过来的currentLikeResourceRegMap
     */
    private HashMap<Integer,ResourceTarget> upperLevelResourceRegMap;

    private HashMap<Integer,ResourceTarget> tempResourceRegMap;


    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * 用于存放需要扫描的JavaClass的白名单
     */
    private static final Set<String> javaClassForScanWhitList = new HashSet<>();

    /**
     * 用于存放不扫描的JavaClass的黑名单
     */
    private static final Set<String> javaClassForScanBlackList = new HashSet<>();

    /**
     * 加载所有定义的资源
     */
    private static final Set<Resource> resourceSet = ResourceFactory.listResources();

    /**
     * operation检测器，检测其是否是资源开启关闭相关的操作
     */
    private static final ResourceOperationDetector detector = new ResourceOperationDetector();

    /**
     * 通过类全限定名可以获得JavaClass的适配器
     */
    private static final AnalysisCacheToRepositoryAdapter adapter = new AnalysisCacheToRepositoryAdapter();

    /**
     * 用于存储lookInto的方法里面的返回值类型，如果是方法里面的资源类型，则返回，如果不是，返回null
     */
    private Boolean currentLookIntoReturnResource = false;

    private static final LinkedStack<Boolean> lookIntoReturnResourceStack = new LinkedStack<>();

    /**
     * 当前层资源变量存储容器
     */
    private ResourceInstanceCapturer currentLevelCapturer = new ResourceInstanceCapturer();

    /**
     * 存储所有层的lookIntoLevelTempCapturer
     */
    private static final LinkedStack<ResourceInstanceCapturer> capturerStack = new LinkedStack<>();

    /**
     * 存储aload出来的变量
     */
    private Integer currentLevelLastRegLoad = null;

    private static final LinkedStack<Integer> lastRegLoadStack = new LinkedStack<>();

    /**
     * lookInto时被指定要扫描的方法
     */
    private Method currentSpecifiedMethod = null;

    /**
     * 用于存放多层lookInto时，指定扫描的方法的栈存储
     */
    private static final LinkedStack<Method> specifiedMethodStack = new LinkedStack<>();

    /**
     * 原始层扫描的标记
     */
    private static final int RAW_SCAN_LEVEL = 0;

    /**
     * lookIntoMethod扫描的标记
     */
    private static final int LOOK_INTO_FOR_OPEN_SCAN_LEVEL = 1;

    private static final int LOOK_INTO_FOR_CLOSE_SCAN_LEVEL = 2;

    private static final String OPEN = "OPEN";

    private static final String CLOSE = "CLOSE";

    private static final String WHITE = "WHITE";

    private static final String BLACK = "BLACK";

    /**
     * key为指定变量资源的操作数，value为已经被关闭的范围
     */
    private Map<Integer,BitSetBuffer> resourceClosed;

    /**
     * 当前扫描层
     */
    private int currentScanLevel = 0;

    /**
     * 当前深入扫描的层次
     */
    private int currentLookIntoLevel = 0;

    /**
     * 最大深入扫描的层次，默认设为3层
     */
    private static final int MAX_LOOK_INTO_LEVEL = ResourceFactory.getMaxLookIntoLevel();

    private static final LinkedStack<Integer> scanLevelStack = new LinkedStack<>();

    @Override
    public boolean atCatchBlock() {
        ClassContext context = getClassContext();
        JavaClass jclass = context.getJavaClass();
        Method method = getMethod();
        BitSet pcInBlock = new BitSet();

        IAnalysisCache analysisCache = Global.getAnalysisCache();
        XMethod xMethod = XFactory.createXMethod(jclass, method);
        OpcodeStack.JumpInfo jumpInfo = null;
        try {
            jumpInfo = analysisCache.getMethodAnalysis(OpcodeStack.JumpInfo.class, xMethod.getMethodDescriptor());
        } catch (CheckedAnalysisException e) {
            AnalysisContext.logError("Error getting jump information", e);
        }
        for (CodeException e : getCode().getExceptionTable()) {
            if (e.getCatchType() != 0) {
                int begin = e.getHandlerPC();
                if (jumpInfo != null) {
                    int end = jumpInfo.getNextJump(begin + 1);
                    if (end >= begin) {
                        pcInBlock.set(begin, end);
                        return pcInBlock.get(getPC());
                    }
                }
            }
        }
        return false;
    }

    public boolean atFinallyBlock() {
        ClassContext context = getClassContext();
        JavaClass jclass = context.getJavaClass();
        Method method = getMethod();
        BitSet pcInCatchBlock = new BitSet();

        IAnalysisCache analysisCache = Global.getAnalysisCache();
        XMethod xMethod = XFactory.createXMethod(jclass, method);
        OpcodeStack.JumpInfo jumpInfo = null;
        try {
            jumpInfo = analysisCache.getMethodAnalysis(OpcodeStack.JumpInfo.class, xMethod.getMethodDescriptor());
        } catch (CheckedAnalysisException e) {
            AnalysisContext.logError("Error getting jump information", e);
        }
        for (CodeException e : getCode().getExceptionTable()) {
            if (e.getCatchType() == 0) {
                int begin = e.getHandlerPC();
                if (jumpInfo != null) {
                    int end = jumpInfo.getNextJump(begin + 1);
                    if (end >= begin) {
                        pcInCatchBlock.set(begin, end);
                        return pcInCatchBlock.get(getPC());
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void visitClassContext(ClassContext classContext) {

        JavaClass jclass = classContext.getJavaClass();
        // for test only
        String name = jclass.getClassName();

        if (DEBUG) {
            System.out.println("============== process debug mode ==============");
        }

        if (javaClassForScanWhitList.contains(jclass.getClassName())) {
            super.visitClassContext(classContext);
            return;
        }

        if (javaClassForScanBlackList.contains(jclass.getClassName())) {
            return;
        }

        for (Constant c : jclass.getConstantPool().getConstantPool()) {
            if (c instanceof ConstantNameAndType) {
                ConstantNameAndType cnt = (ConstantNameAndType) c;
                String signature = cnt.getSignature(jclass.getConstantPool());
                if (ResourceFactory.signatureInvovlesResource(signature)) {
//                    System.out.println("========= file involved with resource =========");
//                    System.out.println("fileName:"+getSourceFile());

                    javaClassForScanWhitList.add(jclass.getClassName());
                    super.visitClassContext(classContext);
                    return;
                }
            } else if (c instanceof ConstantClass) {
                String className = ((ConstantClass) c).getBytes(jclass.getConstantPool());
                if (ResourceFactory.signatureInvovlesResource(className)) {
//                    System.out.println("========= file involved with resource =========");
//                    System.out.println("fileName:"+getSourceFile());

                    javaClassForScanWhitList.add(jclass.getClassName());
                    super.visitClassContext(classContext);
                    return;
                }
            }
        }
        javaClassForScanBlackList.add(jclass.getClassName());
        if (DEBUG) {
            System.out.println(jclass.getClassName() + " isn't interesting for obligation analysis");
        }
    }

    @Override
    public void visit(Code obj) {

        if (resourceSet.isEmpty()) {
            return;
        }

        // 初始化存储容器
        initParam();

        if (currentSpecifiedMethod != null) {
            if (visitingMethod()) {
                if (getMethod().getName().equals(currentSpecifiedMethod.getName())) {
                    super.visit(obj);
                }
            }
        } else {
            super.visit(obj);
            // 遍历currentLevelCapturer里面的resourceInstance，将实例的bugInstance报告掉
            LinkedList<ResourceInstance> instances = currentLevelCapturer.listResourceInstance();
            for (ResourceInstance instance : instances) {
                bugReporter.reportBug(instance.getBugInstance());
            }
            currentLevelCapturer.clear();
        }

    }

    private void initParam() {
        // 初始化参数
        currentLevelCapturer = new ResourceInstanceCapturer();
        blockManager = new IfElseBlockManager();
        branchManager = new IfElseBranchManager();
        resourceClosed = new HashMap<>();
        tempResourceRegMap = new HashMap<>();
    }

    @Override
    public void sawOpcode(int seen) {

        drawIfElseBlock(seen);

        // 如果是最原始的代码扫描层，使用这套逻辑
        if (currentScanLevel == RAW_SCAN_LEVEL) {
            scanCurrentLevel(seen);
        }
        // 如果是深入扫描指定的某个方法的内部寻找资源Open相关信息，则使用这套逻辑
        else if (currentScanLevel == LOOK_INTO_FOR_OPEN_SCAN_LEVEL) {
            scanSubOpenLevel(seen);
        }

        // 如果是深入扫描指定的某个方法的内部寻找资源Close相关信息，则使用这套逻辑
        else if (currentScanLevel == LOOK_INTO_FOR_CLOSE_SCAN_LEVEL) {
            scanSubCloseLevel(seen);
        }
    }

    private void scanSubCloseLevel(int seen) {
        if (seen == ALOAD || seen == ALOAD_0 || seen == ALOAD_1 || seen == ALOAD_2 || seen == ALOAD_3) {
            currentLevelLastRegLoad = getRegisterOperand();
        }

        if (seen == INVOKEVIRTUAL || seen == INVOKESPECIAL || seen == INVOKESTATIC || seen == INVOKEINTERFACE) {
            // 初始化指令码操作对象
            String classConstantOperand = getClassConstantOperand();
            String nameConstantOperand = getNameConstantOperand();
            String signature = getMethodDescriptorOperand().getSignature();
            ResourceOperation targetOperation = new ResourceOperation(classConstantOperand, nameConstantOperand,
                                                                      signature);

            boolean resourceClose = isResourceCloseInvoke(targetOperation);

            if (resourceClose) {


                // 如果lastRegLoad=1，说明关闭的是参数里面的资源
                if (currentSpecifiedMethod.isStatic()) {
                    ResourceTarget resourceTarget = upperLevelResourceRegMap.get(currentLevelLastRegLoad);
                    // 如果有匹配的，则放入tempResourceRegMap
                    if (resourceTarget != null) {
                        resourceTarget.setRealTarget(true);
                        tempResourceRegMap.put(currentLevelLastRegLoad, resourceTarget);
                        currentLookIntoReturnResource = resourceClose;
                    }
                } else {
                    ResourceTarget resourceTarget = upperLevelResourceRegMap.get(currentLevelLastRegLoad - 1);
                    // 如果有匹配的，则放入tempResourceRegMap
                    if (resourceTarget != null) {
                        resourceTarget.setRealTarget(true);
                        tempResourceRegMap.put(currentLevelLastRegLoad, resourceTarget);
                        currentLookIntoReturnResource = resourceClose;
                    }
                }
            }
        }
    }

    private void scanSubOpenLevel(int seen) {
        if (seen == ASTORE || seen == ASTORE_0 || seen == ASTORE_1 || seen == ASTORE_2 || seen == ASTORE_3) {
            int registerOperand = getRegisterOperand();
            boolean addFlag = currentLevelCapturer.addStackIndex(registerOperand);
        }

        if (seen == ALOAD || seen == ALOAD_0 || seen == ALOAD_1 || seen == ALOAD_2 || seen == ALOAD_3) {
            currentLevelLastRegLoad = getRegisterOperand();
        }

        if (seen == ARETURN) {
            // 判断返回的对象是不是开启的资源对象，通过变量的stackIndex（registerOperand）进行判断，
            // 如果是，将资源类型存到lookIntoReturnResource里面
            int prevOpcode = getPrevOpcode(1);
            boolean isLoad = OpcodeUtils.isLoad(prevOpcode);
            if (isLoad) {
                currentLookIntoReturnResource = removeInstance(currentLevelCapturer, currentLevelLastRegLoad);
                currentLevelLastRegLoad = null;
            }
        }

        // 如果在本层发现有资源创建，并作为返回值，则说明有方法，需要返回true，
        // 这个true存在哪里好呢,存在lookIntoResultMap里面
        if (seen == INVOKEVIRTUAL || seen == INVOKESPECIAL || seen == INVOKESTATIC || seen == INVOKEINTERFACE) {
            // 初始化指令码操作对象
            String classConstantOperand = getClassConstantOperand();
            String nameConstantOperand = getNameConstantOperand();
            String signature = getMethodDescriptorOperand().getSignature();
            ResourceOperation targetOperation = new ResourceOperation(classConstantOperand, nameConstantOperand,
                                                                      signature);

            boolean resourceOpen = isResourceOpenInvoke(targetOperation);

            if (resourceOpen) {
                // 需要将资源对象存起来，等到方法走到返回的地方时
                // 查看返回的对象statckIndex是否和记录下来的资源对象的statckIndex相匹配
                // 如果相匹配，将资源对象的种类Resource存起来，怎么存？用栈结构存储
                // 如果nextOpcode是ARETURN，则直接将Resource赋值给lookIntoResource
                int nextOpcode = getNextOpcode();
                if (nextOpcode != ARETURN) {
                    ResourceInstance instance = new ResourceInstance(
                            targetOperation.getInvolvedResourceForOpenInvoke(), null, getPC(), null);
                    currentLevelCapturer.addInstance(instance);
                    blockManager.injectBranchInfo(instance);
                } else {
                    currentLookIntoReturnResource = resourceOpen;
                }

                return;
            }

            boolean resourceClose = isResourceCloseInvoke(targetOperation);

            if (resourceClose) {
                removeInstance(currentLevelCapturer, currentLevelLastRegLoad);
                currentLevelLastRegLoad = null;
            }
        }
    }

    private void scanCurrentLevel(int seen) {
        if (seen == ASTORE || seen == ASTORE_0 || seen == ASTORE_1 || seen == ASTORE_2 || seen == ASTORE_3) {
            // 如果其前面一条指令是invoke，则存储registerOperand
            int prevOpcode = getPrevOpcode(1);
            boolean isInvoke = OpcodeUtils.isInvoke(prevOpcode);
            if (isInvoke) {
                int registerOperand = getRegisterOperand();
                currentLevelCapturer.addStackIndex(registerOperand);
            }
        }

        if (seen == ALOAD || seen == ALOAD_0 || seen == ALOAD_1 || seen == ALOAD_2 || seen == ALOAD_3) {
            currentLevelLastRegLoad = getRegisterOperand();
        }

        if (seen == ARETURN) {
            // 判断返回的对象是不是开启的资源对象，通过变量的stackIndex（registerOperand）进行判断，
            // 如果是，将其从capturer中消除（相当于关闭资源）
            int prevOpcode = getPrevOpcode(1);
            boolean isLoad = OpcodeUtils.isLoad(prevOpcode);
            if (isLoad) {
                removeInstance(currentLevelCapturer, currentLevelLastRegLoad);
            }
        }

        if (seen == INVOKEVIRTUAL || seen == INVOKESPECIAL || seen == INVOKESTATIC || seen == INVOKEINTERFACE) {
            // 初始化指令码操作对象
            String classConstantOperand = getClassConstantOperand();
            String nameConstantOperand = getNameConstantOperand();
            String signature = getMethodDescriptorOperand().getSignature();
            ResourceOperation targetOperation = new ResourceOperation(classConstantOperand, nameConstantOperand,
                                                                      signature);

            // 如果是开启资源的方法，则建立资源实例，存储到ResourceInstanceCapturer当中去
            boolean resourceOpen = isResourceOpenInvoke(targetOperation);
            if (resourceOpen) {
                int nextOpcode = getNextOpcode();
                // (加一个前提条件后面的一个操作码不是ARETURN)
                if (nextOpcode != ARETURN) {
                    BugInstance bugInstance = new BugInstance(this, "RESOURCE_NOT_RELEASED", HIGH_PRIORITY)
                            .addClassAndMethod(this).addSourceLine(this, getPC());
                    ResourceInstance resourceInstance = new ResourceInstance(
                            targetOperation.getInvolvedResourceForOpenInvoke(), null, getPC(), bugInstance);
                    // 并且要把ResourceInstanceCapturer的valve打开，方便下个指令码扫描时，加入stackIndex
                    currentLevelCapturer.addInstance(resourceInstance);

                    blockManager.injectBranchInfo(resourceInstance);
                }
                return;
            }

            // 如果是关闭资源的方法，则将资源从ResourceInstanceCapturer中去除，需要知道变量的statckIndex
            boolean resourceClose = isResourceCloseInvoke(targetOperation);

            if (resourceClose) {
                removeInstance(currentLevelCapturer, currentLevelLastRegLoad);
                currentLevelLastRegLoad = null;
            }
        }
    }

    private boolean inIfNullBlock(BitSet range) {
        int start = range.nextSetBit(0);
        int prevOpcode = getPrevOpcode(start);
        if (prevOpcode == IFNULL) {
            return true;
        }
        return false;
    }

    // 绘制IfElseBlock结构图
    private void drawIfElseBlock(int seen) {
        // 记录区间
        if (OpcodeUtils.isIfInstruction(seen)) {
            int branchTarget = getBranchTarget();
            int branchFallThrough = getBranchFallThrough();

            BitSet bitSet = new BitSet();
            if (branchFallThrough < branchTarget) {
                bitSet.set(branchFallThrough, branchTarget);

                IfElseBranch branch = new IfElseBranch(branchFallThrough, branchTarget, seen);
                IfElseBlock parent = blockManager.getParent(branch);
                if (parent != null) {
                    parent.addBranch(branch);
                } else {
                    IfElseBlock block2 = new IfElseBlock();
                    block2.addBranch(branch);
                    blockManager.addBlock(block2);
                }
            }
        } else {
            // 记录goto信息

            int pc = getPC();
            Integer nextPC = null;
            if (getPC() >= getMaxPC()) {
                return;
            }
            nextPC = getNextPC();

            // 更新goto信息
            LinkedList<IfElseBranch> branchList = blockManager.getBranchByBranchEnd(nextPC);

            for (IfElseBranch branch : branchList) {

                if (seen == GOTO) {
                    branch.setGotoTarget(getBranchTarget());
                }
            }
        }
    }


    /**
     * 关闭对应的资源
     *
     * @param currentLevelCapturer
     * @param currentLevelLastRegLoad
     */
    private boolean removeInstance(ResourceInstanceCapturer currentLevelCapturer,
                                   Integer currentLevelLastRegLoad) {

        // 判断执行语句是否是在catch块中，如果是，则不进行操作
        boolean atCatchBlock = atCatchBlock();
        if (atCatchBlock) {
            return false;
        }

        int pc = getPC();

//        System.out.println("Close instruction pc:["+pc+"]");
        
        // 获取close的作用范围
        BitSetBuffer closeRange = new BitSetBuffer();
        TreeMap<BitSetBuffer, Integer> exist = blockManager.getExistRanges(pc);

        if (exist != null) {
            // 从小到大遍历所有范围，取不是IFNULL为条件的最小范围
            for (Map.Entry<BitSetBuffer, Integer> entry : exist.entrySet()) {
                Integer opcode = entry.getValue();
                if (opcode != null && opcode != IFNULL) {
                    closeRange.or(entry.getKey());
                    break;
                }
            }
        }


        // 当然，closeRange应该到pc位置就结束了，后面部分要截掉
        if (!closeRange.isEmpty()) {
            Integer end = closeRange.getEnd();
            BitSetBuffer set = new BitSetBuffer();
            set.set(pc, end);
            closeRange.andNot(set);
        }else {
            closeRange.set(0, pc);
        }

        BitSetBuffer hasClosed = resourceClosed.get(currentLevelLastRegLoad);
        if (hasClosed != null) {
            closeRange.andNot(hasClosed);
        }else {
            hasClosed = new BitSetBuffer();
        }

        // todo
//        System.out.println("CloseRange:"+closeRange);

        boolean flag = currentLevelCapturer.removeInstance(currentLevelLastRegLoad, closeRange);

        if (flag) {
            // 如果关闭成功，则将closeRange加入到hasClosed范围里面去
            hasClosed.or(closeRange);
            resourceClosed.put(currentLevelLastRegLoad,closeRange);
//            System.out.println("Close success...");
//            System.out.println("=========================================");
            return true;
        }else {
//            System.out.println("Close failed...");
//            System.out.println("=========================================");
        }

        return false;
    }

    private boolean hasNextPC() {
        if(getPC() < getMaxPC()){
            return true;
        }
        return false;
    }

    // 思路：
    // 返回值必须匹配，相同或者是Resource的父类

    /**
     * @param targetOperation 目标操作
     * @return
     */
    private boolean likeResourceOpenInvoke(ResourceOperation targetOperation) {
        // 从signature里面取到返回类型的部分
        String signature = targetOperation.getSignature();
        String className = SignatureUtils.getObjectReturnTypeClassName(signature);
        if (className == null) {
            return false;
        }

        ResourceMacher resourceMacher = new ResourceMacher(className);
        return resourceMacher.matches();
    }

    // 思路：
    // 如果signature是以（资源）V形式存在，则可能是关闭资源的方法，
    // 并且参数里面的资源是之前已经Open过的

    /**
     * 判断指令操作想不想资源关闭的操作
     *
     * @param targetOperation
     * @return
     */
    private boolean likeResourceCloseInvoke(ResourceOperation targetOperation){

        String methodName = targetOperation.getMethodName();
        if ("<init>".equals(methodName)) {
            return false;
        }
        String signature = targetOperation.getSignature();
        SignatureParser parser = new SignatureParser(signature);
        String[] arguments = parser.getArguments();

        // 初始化currentLikeResourceRegMap
        currentLikeResourceRegMap = new HashMap<>();
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            boolean invovlesResource = ResourceFactory.signatureInvovlesResource(argument);
            if (invovlesResource) {
                String paramClassName = SignatureUtils.trimArgument(argument);
                currentLikeResourceRegMap.put(i, new ResourceTarget(paramClassName));
            }
        }

        if (currentLikeResourceRegMap.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断指令是否是用于开启资源的
     *
     * @return
     */
    private boolean isResourceOpenInvoke(ResourceOperation targetOperation) {
        // 如果在目前已有的方法库里面能够匹配到，则返回true，不然继续进一步判断
        boolean inOpenWhiteList = detector.inOpenWhiteList(targetOperation);
        if (inOpenWhiteList) {
            return true;
        }
        boolean inOpenBlackList = detector.inOpenBlackList(targetOperation);
        if (inOpenBlackList) {
            return false;
        }

        // 如果不像是资源开启方法，直接返回false
        if (likeResourceOpenInvoke(targetOperation)) {
            boolean isResourceOpen = lookIntoMethodWraper(targetOperation, LOOK_INTO_FOR_OPEN_SCAN_LEVEL);
            String openClassName = SignatureUtils.getObjectReturnTypeClassName(targetOperation.getSignature());
            if (isResourceOpen) {
                if (openClassName != null) {
                    detector.appendOperation(openClassName, targetOperation, OPEN, WHITE);
                }
                return true;
            } else {
                if (currentLookIntoLevel == 0) {
                    detector.appendOperation(OPEN, targetOperation, OPEN, BLACK);
                }
            }
        }

        return false;
    }

    /**
     * 判断方法
     *
     * @param targetOperation
     * @return
     */
    private boolean isResourceCloseInvoke(ResourceOperation targetOperation) {
        // 如果在目前已有的方法库里面能够匹配到，则返回true，不然继续进一步判断
        boolean inCloseWhiteList = detector.inCloseWhitList(targetOperation);
        if (inCloseWhiteList) {
            return true;
        }
        boolean inCloseBlackList = detector.inCloseBlackList(targetOperation);
        if (inCloseWhiteList) {
            return false;
        }

        // 思路同Open方法
        if (likeResourceCloseInvoke(targetOperation)) {
            boolean isResourceClose = lookIntoMethodWraper(targetOperation, LOOK_INTO_FOR_CLOSE_SCAN_LEVEL);
//            String closeClassName = SignatureUtils.getObjectParamClassName(targetOperation.getSignature());
            if (isResourceClose) {
                // 遍历currentLikeResourceRegMap，将信息加入closeWhiteList
                for (ResourceTarget resourceTarget : currentLikeResourceRegMap.values()) {
                    boolean realTarget = resourceTarget.isRealTarget();
                    String className = resourceTarget.getClassName();
                    if (realTarget) {
                        detector.appendOperation(className, targetOperation, CLOSE, WHITE);
                    }
                }
//                if (closeClassName != null) {
//                    detector.appendOperation(closeClassName, targetOperation, CLOSE, WHITE);
//                }
                return true;
            } else {
                if (currentLookIntoLevel == 0) {
                    detector.appendOperation(CLOSE, targetOperation, CLOSE, BLACK);
                }
            }
        }
        return false;
    }

    /**
     * 对lookIntoMethod做了栈操作的增强
     *
     * @param targetOperation
     * @param scanLevel
     * @return
     */
    private boolean lookIntoMethodWraper(ResourceOperation targetOperation, int scanLevel) {
        // 到方法内部看看，如果里面存在创建资源，且将资源作为返回结果的，则返回对应资源的种类，不然返回null
        // lookInto之前，要将lookIntoLevelTempCapturer以及currentLevelLastRegLoads进行入栈操作，
        capturerStack.push(currentLevelCapturer);
        lastRegLoadStack.push(currentLevelLastRegLoad);
        lookIntoReturnResourceStack.push(currentLookIntoReturnResource);
        specifiedMethodStack.push(currentSpecifiedMethod);
        scanLevelStack.push(currentScanLevel);
        blockManagerStack.push(blockManager);
        branchManagerStack.push(branchManager);

        boolean resource = false;
        try {
            resource = lookIntoMethod(targetOperation, scanLevel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // 并在lookInto方法里面操作完毕后进行出栈操作
            currentLevelCapturer = capturerStack.pop();
            currentLevelLastRegLoad = lastRegLoadStack.pop();
            currentLookIntoReturnResource = lookIntoReturnResourceStack.pop();
            currentSpecifiedMethod = specifiedMethodStack.pop();
            currentScanLevel = scanLevelStack.pop();
            blockManager = blockManagerStack.pop();
            branchManager = branchManagerStack.pop();
        }

        return resource;
    }

    /**
     * 用于检查方法里面有无创建资源并返回，如果有，返回true
     *
     * @param targetOperation 目标检测操作
     * @param scanLevel
     * @return
     */
    // lookInto之后，currentCapture要重新建立，lookInto结束之后，当前的currentCapture要销毁
    private boolean lookIntoMethod(ResourceOperation targetOperation, Integer scanLevel) {
        try {
            currentLookIntoLevel++;
            if (currentLookIntoLevel > MAX_LOOK_INTO_LEVEL) {
                return false;
            }
            JavaClass aClass = adapter.findClass(targetOperation.getClazzName());
            ClassDescriptor classDescriptor = DescriptorFactory.createClassDescriptor(aClass);
            ClassContext classContext = analysisCache.getClassAnalysis(ClassContext.class, classDescriptor);
            Method[] methods = aClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(targetOperation.getMethodName())) {
                    Code lookIntoCode = method.getCode();
                    currentScanLevel = scanLevel;
                    currentSpecifiedMethod = method;
                    BadResourceCheck badResourceCheck = new BadResourceCheck(bugReporter,
                                                                             currentScanLevel,
                                                                             currentSpecifiedMethod,
                                                                             currentLookIntoLevel,
                                                                             currentLikeResourceRegMap);
                    badResourceCheck.visitClassContext(classContext);

                    // 将深入扫描得到的tempResourceRegMap（里层）赋值给currentLikeResourceRegMap
                    currentLikeResourceRegMap.clear();
                    currentLikeResourceRegMap.putAll(badResourceCheck.tempResourceRegMap);

                    // 如果是满足条件的资源开启或者关闭操作，则返回这个资源类型Resource
                    return badResourceCheck.currentLookIntoReturnResource;
                }
            }
            return false;
        } catch (Exception e) {
            //logger.warning("Check skipped! Class file not found:"+targetOperation.getClazzName()+".");
        } finally {
            currentLookIntoLevel--;
        }
        return false;
    }

    private Integer getLineNumber() {
        return lineNumberTable.getSourceLine(getPC());
    }
}
