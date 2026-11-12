package cl.esucar.tutorvirtual.ui.stages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.esucar.tutorvirtual.di.appViewModel
import cl.esucar.tutorvirtual.ui.components.AppTopBar
import cl.esucar.tutorvirtual.ui.components.ContentWidth
import cl.esucar.tutorvirtual.ui.components.EmptyState

@Composable
fun GuideScreen(onBack: () -> Unit, vm: GuideViewModel = appViewModel()) {
    val stage by vm.stage.collectAsStateWithLifecycle()
    val steps by vm.steps.collectAsStateWithLifecycle()
    val index by vm.index.collectAsStateWithLifecycle()

    val total = steps.size
    val current = index.coerceIn(0, (total - 1).coerceAtLeast(0))
    val isLast = current == total - 1
    val progress by animateFloatAsState(
        if (total == 0) 0f else (current + 1f) / total, tween(400), label = "guideProgress"
    )

    Scaffold(topBar = { AppTopBar(stage?.title ?: "Guía", onBack) }) { pad ->
        ContentWidth(Modifier.padding(pad)) {
            if (total == 0) {
                EmptyState(Icons.AutoMirrored.Filled.MenuBook, "Esta etapa aún no tiene pasos.")
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        "Paso ${current + 1} de $total",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = current,
                        modifier = Modifier.weight(1f),
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))) togetherWith
                                    (slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(200)))
                            }
                        },
                        label = "guideStep"
                    ) { i ->
                        val step = steps[i.coerceIn(0, steps.lastIndex)]
                        Card(
                            Modifier.fillMaxSize(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                                Text(step.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(12.dp))
                                Text(step.content, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = vm::prev, enabled = current > 0,
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Anterior")
                        }
                        Button(
                            onClick = { if (isLast) onBack() else vm.next() },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) {
                            Text(if (isLast) "Finalizar" else "Siguiente")
                            Spacer(Modifier.size(6.dp))
                            Icon(if (isLast) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
