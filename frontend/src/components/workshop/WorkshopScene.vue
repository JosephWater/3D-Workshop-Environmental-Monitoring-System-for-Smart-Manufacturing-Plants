<script setup>
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  sensors: {
    type: Array,
    default: () => [],
  },
  selectedSensorCode: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['select'])

const containerRef = ref(null)

let renderer
let scene
let camera
let controls
let animationFrameId
let raycaster
let pointer
let resizeObserver
let workshopGroup
let sensorGroup
let sensorMeshes = new Map()

function createRenderer() {
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
}

function createScene() {
  scene = new THREE.Scene()
  scene.background = new THREE.Color('#f3f4ef')
  scene.fog = new THREE.Fog('#f3f4ef', 55, 92)

  camera = new THREE.PerspectiveCamera(42, 1, 0.1, 220)
  camera.position.set(40, 28, 34)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.minDistance = 20
  controls.maxDistance = 95
  controls.target.set(0, 5, 0)
  controls.maxPolarAngle = Math.PI / 2.05

  workshopGroup = new THREE.Group()
  sensorGroup = new THREE.Group()

  const ambientLight = new THREE.AmbientLight('#ffffff', 1.3)

  const keyLight = new THREE.DirectionalLight('#fffef2', 1.3)
  keyLight.position.set(22, 30, 18)
  keyLight.castShadow = true
  keyLight.shadow.mapSize.set(2048, 2048)

  const fillLight = new THREE.DirectionalLight('#d8e2ef', 0.6)
  fillLight.position.set(-22, 18, -12)

  scene.add(ambientLight, keyLight, fillLight, workshopGroup, sensorGroup)
}

function addMesh(parent, geometry, material, position, castShadow = true, receiveShadow = true) {
  const mesh = new THREE.Mesh(geometry, material)
  mesh.position.copy(position)
  mesh.castShadow = castShadow
  mesh.receiveShadow = receiveShadow
  parent.add(mesh)
  return mesh
}

function buildProceduralWorkshop() {
  workshopGroup.clear()

  const floorMaterial = new THREE.MeshStandardMaterial({
    color: '#e0e4ea',
    roughness: 0.9,
    metalness: 0.04,
  })
  addMesh(workshopGroup, new THREE.BoxGeometry(46, 1.2, 46), floorMaterial, new THREE.Vector3(0, -0.6, 0))

  const wallMaterial = new THREE.MeshStandardMaterial({
    color: '#eef1f6',
    roughness: 0.9,
    metalness: 0.03,
    transparent: true,
    opacity: 0.9,
  })

  addMesh(workshopGroup, new THREE.BoxGeometry(46, 12, 0.7), wallMaterial, new THREE.Vector3(0, 5.6, -23))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.7, 12, 46), wallMaterial, new THREE.Vector3(-23, 5.6, 0))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.7, 12, 46), wallMaterial, new THREE.Vector3(23, 5.6, 0))

  const roofFrameMaterial = new THREE.MeshStandardMaterial({
    color: '#a5adba',
    roughness: 0.76,
    metalness: 0.24,
  })
  for (let x = -18; x <= 18; x += 9) {
    addMesh(workshopGroup, new THREE.BoxGeometry(0.4, 1.4, 46), roofFrameMaterial, new THREE.Vector3(x, 11.2, 0))
  }
  for (let z = -18; z <= 18; z += 9) {
    addMesh(workshopGroup, new THREE.BoxGeometry(46, 0.35, 0.35), roofFrameMaterial, new THREE.Vector3(0, 11.6, z))
  }

  const tileLineMaterial = new THREE.LineBasicMaterial({ color: '#c7ced8' })
  for (let offset = -20; offset <= 20; offset += 4) {
    const vertical = new THREE.BufferGeometry().setFromPoints([
      new THREE.Vector3(offset, 0.08, -22),
      new THREE.Vector3(offset, 0.08, 22),
    ])
    const horizontal = new THREE.BufferGeometry().setFromPoints([
      new THREE.Vector3(-22, 0.08, offset),
      new THREE.Vector3(22, 0.08, offset),
    ])
    workshopGroup.add(new THREE.Line(vertical, tileLineMaterial))
    workshopGroup.add(new THREE.Line(horizontal, tileLineMaterial))
  }

  const laneMaterial = new THREE.MeshStandardMaterial({
    color: '#aeb3bc',
    roughness: 0.88,
    metalness: 0.08,
  })
  const lane = addMesh(
    workshopGroup,
    new THREE.BoxGeometry(33, 0.14, 12),
    laneMaterial,
    new THREE.Vector3(-2, 0.06, 3),
    false,
    true,
  )
  lane.rotation.y = -0.56

  const lineGuideMaterial = new THREE.LineBasicMaterial({ color: '#f1f4f8' })
  ;[
    [
      new THREE.Vector3(-15.5, 0.14, -4.6),
      new THREE.Vector3(9.2, 0.14, 11.1),
    ],
    [
      new THREE.Vector3(-17.5, 0.14, -1.4),
      new THREE.Vector3(7.2, 0.14, 14.3),
    ],
    [
      new THREE.Vector3(-19.5, 0.14, 1.8),
      new THREE.Vector3(5.2, 0.14, 17.5),
    ],
  ].forEach((points) => {
    workshopGroup.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints(points), lineGuideMaterial))
  })

  const pressConfigs = [
    { x: -10.5, z: -4.4, label: 'B21' },
    { x: -4.3, z: -0.5, label: 'B22' },
    { x: 1.8, z: 3.4, label: 'B23' },
    { x: 8.1, z: 7.4, label: 'B24' },
  ]

  pressConfigs.forEach((machine) => {
    const silverMat = new THREE.MeshStandardMaterial({
      color: '#d1d7e0',
      roughness: 0.5,
      metalness: 0.42,
    })
    const darkSilverMat = new THREE.MeshStandardMaterial({
      color: '#8d96a3',
      roughness: 0.52,
      metalness: 0.38,
    })
    const amberMat = new THREE.MeshStandardMaterial({
      color: '#f1bd36',
      roughness: 0.48,
      metalness: 0.16,
    })
    const blueMat = new THREE.MeshStandardMaterial({
      color: '#3f8ce3',
      roughness: 0.5,
      metalness: 0.14,
    })

    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(5.6, 0.65, 3.2),
      new THREE.MeshStandardMaterial({ color: '#d7dbe3', roughness: 0.82 }),
      new THREE.Vector3(machine.x, 0.32, machine.z),
    )
    addMesh(workshopGroup, new THREE.BoxGeometry(4, 0.55, 2.2), blueMat, new THREE.Vector3(machine.x, 0.94, machine.z))
    addMesh(workshopGroup, new THREE.BoxGeometry(4.6, 0.36, 2.7), amberMat, new THREE.Vector3(machine.x + 1.2, 1.36, machine.z))
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(5.1, 3.4, 2.8),
      silverMat,
      new THREE.Vector3(machine.x - 0.5, 3.05, machine.z),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(2.4, 2.8, 1.4),
      darkSilverMat,
      new THREE.Vector3(machine.x + 1.3, 2.7, machine.z),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(1.1, 4.8, 0.8),
      darkSilverMat,
      new THREE.Vector3(machine.x - 0.9, 5.2, machine.z),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(0.58, 5.2, 0.58),
      silverMat,
      new THREE.Vector3(machine.x - 2.1, 5.1, machine.z - 1.02),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(0.58, 5.2, 0.58),
      silverMat,
      new THREE.Vector3(machine.x - 2.1, 5.1, machine.z + 1.02),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(0.58, 2.3, 0.58),
      silverMat,
      new THREE.Vector3(machine.x + 1.9, 3.65, machine.z - 1.05),
    )
    addMesh(
      workshopGroup,
      new THREE.BoxGeometry(0.58, 2.3, 0.58),
      silverMat,
      new THREE.Vector3(machine.x + 1.9, 3.65, machine.z + 1.05),
    )

    for (let finOffset = -1.12; finOffset <= 1.12; finOffset += 0.37) {
      addMesh(
        workshopGroup,
        new THREE.BoxGeometry(0.1, 1, 0.12),
        darkSilverMat,
        new THREE.Vector3(machine.x + 2.05, 3.3, machine.z + finOffset),
      )
    }

    const cable = addMesh(
      workshopGroup,
      new THREE.CylinderGeometry(0.06, 0.06, 5.8, 10),
      new THREE.MeshStandardMaterial({ color: '#737b84', roughness: 0.7, metalness: 0.16 }),
      new THREE.Vector3(machine.x + 3.25, 3.2, machine.z),
    )
    cable.rotation.z = 0.08
  })

  const glassMat = new THREE.MeshStandardMaterial({
    color: '#e8f0f8',
    transparent: true,
    opacity: 0.22,
    roughness: 0.1,
    metalness: 0.02,
  })
  const frameMat = new THREE.MeshStandardMaterial({
    color: '#98a1ad',
    roughness: 0.72,
    metalness: 0.22,
  })
  addMesh(workshopGroup, new THREE.BoxGeometry(0.12, 5.2, 7.8), glassMat, new THREE.Vector3(13.2, 2.6, 10.8))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.16, 5.5, 0.16), frameMat, new THREE.Vector3(13.0, 2.75, 7.1))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.16, 5.5, 0.16), frameMat, new THREE.Vector3(13.0, 2.75, 14.5))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.16, 5.5, 0.16), frameMat, new THREE.Vector3(13.4, 2.75, 7.1))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.16, 5.5, 0.16), frameMat, new THREE.Vector3(13.4, 2.75, 14.5))

  addMesh(
    workshopGroup,
    new THREE.BoxGeometry(12.6, 0.9, 2.8),
    new THREE.MeshStandardMaterial({ color: '#44474d', roughness: 0.78, metalness: 0.12 }),
    new THREE.Vector3(18.2, 0.46, 13.5),
  )
  addMesh(
    workshopGroup,
    new THREE.BoxGeometry(12.6, 0.14, 3.2),
    new THREE.MeshStandardMaterial({ color: '#b9c0cb', roughness: 0.82, metalness: 0.08 }),
    new THREE.Vector3(18.2, 0.98, 13.5),
  )
  for (let x = 13; x <= 23; x += 2.5) {
    addMesh(
      workshopGroup,
      new THREE.CylinderGeometry(0.12, 0.12, 1.1, 12),
      frameMat,
      new THREE.Vector3(x, 0.35, 15.2),
    )
  }

  const pillarMat = new THREE.MeshStandardMaterial({
    color: '#8e97a3',
    roughness: 0.82,
    metalness: 0.24,
    transparent: true,
    opacity: 0.38,
  })
  addMesh(workshopGroup, new THREE.BoxGeometry(2.6, 9.8, 2.6), pillarMat, new THREE.Vector3(15.6, 4.9, -8.4))
  addMesh(workshopGroup, new THREE.BoxGeometry(0.16, 10.8, 8), frameMat, new THREE.Vector3(15.6, 5.4, -8.4))
}

async function loadExternalModel() {
  const loader = new GLTFLoader()
  try {
    const gltf = await loader.loadAsync('/models/workshop.glb')
    workshopGroup.clear()
    const model = gltf.scene
    model.traverse((child) => {
      if (child.isMesh) {
        child.castShadow = true
        child.receiveShadow = true
      }
    })
    model.scale.setScalar(1.8)
    model.position.set(0, 0, 0)
    workshopGroup.add(model)
  } catch {
    buildProceduralWorkshop()
  }
}

function rebuildSensors() {
  sensorGroup.children.forEach((child) => {
    child.geometry?.dispose?.()
    child.material?.dispose?.()
  })
  sensorGroup.clear()
  sensorMeshes = new Map()

  props.sensors.forEach((sensor) => {
    const sensorColor = sensor.alarmActive ? '#e18a2f' : '#6fd4ff'

    const pole = addMesh(
      sensorGroup,
      new THREE.CylinderGeometry(0.09, 0.09, 1.8, 12),
      new THREE.MeshStandardMaterial({ color: '#8d97a4', roughness: 0.72, metalness: 0.18 }),
      new THREE.Vector3(sensor.x, 0.9, sensor.z),
    )
    pole.userData.sensorCode = sensor.sensorCode

    const beacon = addMesh(
      sensorGroup,
      new THREE.SphereGeometry(0.95, 28, 28),
      new THREE.MeshStandardMaterial({
        color: sensorColor,
        emissive: sensorColor,
        emissiveIntensity: sensor.alarmActive ? 0.52 : 0.16,
        roughness: 0.36,
        metalness: 0.16,
      }),
      new THREE.Vector3(sensor.x, sensor.y + 1.5, sensor.z),
    )
    beacon.userData.sensorCode = sensor.sensorCode

    const ring = addMesh(
      sensorGroup,
      new THREE.TorusGeometry(1.4, 0.08, 12, 36),
      new THREE.MeshStandardMaterial({
        color: sensor.alarmActive ? '#ffd2a0' : '#b7f0ff',
        roughness: 0.55,
        metalness: 0.06,
      }),
      new THREE.Vector3(sensor.x, 0.18, sensor.z),
      false,
      true,
    )
    ring.rotation.x = Math.PI / 2
    ring.userData.sensorCode = sensor.sensorCode

    sensorMeshes.set(sensor.sensorCode, { beacon, ring })
  })

  applySelection()
}

function applySelection() {
  sensorMeshes.forEach((meshes, code) => {
    const sensor = props.sensors.find((item) => item.sensorCode === code)
    const activeColor = sensor?.alarmActive ? '#e18a2f' : '#6fd4ff'
    const selected = code === props.selectedSensorCode
    meshes.beacon.scale.setScalar(selected ? 1.28 : 1)
    meshes.beacon.material.color.set(selected ? '#111827' : activeColor)
    meshes.ring.scale.setScalar(selected ? 1.16 : 1)
  })
}

function resizeRenderer() {
  if (!containerRef.value || !renderer || !camera) {
    return
  }
  const { clientWidth, clientHeight } = containerRef.value
  renderer.setSize(clientWidth, clientHeight)
  camera.aspect = clientWidth / clientHeight
  camera.updateProjectionMatrix()
}

function animate() {
  const t = performance.now() * 0.0025
  sensorMeshes.forEach((meshes, code) => {
    const sensor = props.sensors.find((item) => item.sensorCode === code)
    const pulse = sensor?.alarmActive ? 0.38 + Math.abs(Math.sin(t * 3 + meshes.beacon.position.x * 0.08)) * 0.84 : 0.16
    meshes.beacon.material.emissiveIntensity = pulse
    meshes.beacon.position.y = (sensor?.y ?? 0) + 1.5 + Math.sin(t + meshes.beacon.position.x * 0.16) * 0.08
    meshes.ring.material.opacity = 0.92
  })

  controls?.update()
  renderer.render(scene, camera)
  animationFrameId = window.requestAnimationFrame(animate)
}

function onPointerDown(event) {
  if (!containerRef.value) {
    return
  }
  const rect = containerRef.value.getBoundingClientRect()
  pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
  raycaster.setFromCamera(pointer, camera)

  const selectable = [
    ...sensorGroup.children.filter((child) => child.userData?.sensorCode),
  ]
  const intersects = raycaster.intersectObjects(selectable)
  if (intersects.length > 0) {
    emit('select', intersects[0].object.userData.sensorCode)
  }
}

function resetView() {
  camera.position.set(26, 32, 28)
  controls.target.set(0, 4.5, 2.5)
  controls.update()
}

onMounted(async () => {
  createRenderer()
  createScene()
  raycaster = new THREE.Raycaster()
  pointer = new THREE.Vector2()
  containerRef.value.appendChild(renderer.domElement)
  await loadExternalModel()
  rebuildSensors()
  resizeRenderer()
  animate()

  resizeObserver = new ResizeObserver(() => resizeRenderer())
  resizeObserver.observe(containerRef.value)
  containerRef.value.addEventListener('pointerdown', onPointerDown)
})

watch(
  () => props.sensors,
  () => {
    if (sensorGroup) {
      rebuildSensors()
    }
  },
  { deep: true },
)

watch(
  () => props.selectedSensorCode,
  () => {
    if (sensorGroup) {
      applySelection()
    }
  },
)

onBeforeUnmount(() => {
  if (animationFrameId) {
    window.cancelAnimationFrame(animationFrameId)
  }
  if (resizeObserver && containerRef.value) {
    resizeObserver.unobserve(containerRef.value)
  }
  if (containerRef.value) {
    containerRef.value.removeEventListener('pointerdown', onPointerDown)
  }
  controls?.dispose()
  renderer?.dispose()
})
</script>

<template>
  <div ref="containerRef" class="scene-shell">
    <div class="scene-shell__hud">
      <div class="scene-shell__legend">
        <span><i class="legend-dot legend-dot--ok" /> 正常</span>
        <span><i class="legend-dot legend-dot--danger" /> 告警</span>
      </div>
      <p>左键旋转，滚轮缩放，右键平移</p>
      <button class="button button--tiny" type="button" @click.stop="resetView">重置视角</button>
    </div>
  </div>
</template>
